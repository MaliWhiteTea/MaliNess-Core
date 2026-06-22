package com.mertaliakcay.malinesscore.gui.content;

import com.mertaliakcay.malinesscore.gui.model.MenuClickType;
import com.mertaliakcay.malinesscore.gui.model.MenuSession;
import com.mertaliakcay.malinesscore.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class DemoPwarpContentProvider implements MenuContentProvider {

    private final boolean empty;

    public DemoPwarpContentProvider(boolean empty) {
        this.empty = empty;
    }

    @Override
    public String getId() {
        return empty ? "demo-pwarp-empty" : "demo-pwarp";
    }

    @Override
    public void initialize(MenuSession session) {
        if (empty) {
            session.getProviderState().put("entries", List.<DemoEntry>of());
            return;
        }
        List<DemoEntry> entries = new ArrayList<>();
        String playerName = playerName(session);
        for (int index = 1; index <= 65; index++) {
            String owner = index <= 3 ? playerName : "Oyuncu" + ((index % 6) + 1);
            entries.add(new DemoEntry(
                    "warp-" + index,
                    "DemoWarp" + index,
                    owner,
                    index * 3,
                    index % 5 == 0
            ));
        }
        session.getProviderState().put("entries", entries);
    }

    @Override
    public int getTotalPages(MenuSession session) {
        List<DemoEntry> entries = filteredEntries(session);
        int perPage = Math.max(1, session.getDefinition().getContentSlots().size());
        return Math.max(1, (int) Math.ceil(entries.size() / (double) perPage));
    }

    @Override
    public void populatePage(MenuSession session) {
        List<DemoEntry> entries = filteredEntries(session);
        int perPage = session.getDefinition().getContentSlots().size();
        int start = (session.getCurrentPage() - 1) * perPage;

        List<ItemStack> stacks = new ArrayList<>();
        List<String> entryIds = new ArrayList<>();

        for (int index = start; index < Math.min(start + perPage, entries.size()); index++) {
            DemoEntry entry = entries.get(index);
            stacks.add(createHead(entry));
            entryIds.add(entry.id());
        }

        session.getProviderState().put("page-items", stacks);
        session.getProviderState().put("page-entry-ids", entryIds);
    }

    @Override
    public void onContentClick(Player player, MenuSession session, String entryId, MenuClickType clickType) {
        player.sendMessage(ColorUtil.colorize(
                "&7[Demo] &f" + entryId + " &7→ &e" + clickType.toYamlKey()
        ));
    }

    @SuppressWarnings("unchecked")
    private List<DemoEntry> filteredEntries(MenuSession session) {
        List<DemoEntry> entries = (List<DemoEntry>) session.getProviderState().getOrDefault("entries", List.of());
        List<DemoEntry> filtered = new ArrayList<>(entries);

        filtered.removeIf(entry -> switch (session.getFilterMode()) {
            case FAVORITES -> !entry.favorite();
            case MINE -> !entry.owner().equalsIgnoreCase(playerName(session));
            case ALL -> false;
        });

        Comparator<DemoEntry> comparator = switch (session.getSortMode()) {
            case NAME -> Comparator.comparing(entry -> entry.name().toLowerCase(Locale.ROOT));
            case RECENT -> Comparator.comparingInt(DemoEntry::idNumber).reversed();
            case VISITS -> Comparator.comparingInt(DemoEntry::visits).reversed();
        };
        filtered.sort(comparator);
        return filtered;
    }

    private String playerName(MenuSession session) {
        Player player = Bukkit.getPlayer(session.getPlayerId());
        return player != null ? player.getName() : "Oyuncu1";
    }

    private ItemStack createHead(DemoEntry entry) {
        ItemStack stack = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorUtil.colorizePlain("&e" + entry.name()));
            meta.setLore(List.of(
                    ColorUtil.colorizePlain("&7Sahip: &f" + entry.owner()),
                    ColorUtil.colorizePlain("&7Ziyaret: &f" + entry.visits()),
                    ColorUtil.colorizePlain("&8Demo pwarp")
            ));
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private record DemoEntry(String id, String name, String owner, int visits, boolean favorite) {
        int idNumber() {
            try {
                return Integer.parseInt(id.replace("warp-", ""));
            } catch (NumberFormatException exception) {
                return 0;
            }
        }
    }
}
