package com.mertaliakcay.malinesscore.gui.content;

import com.mertaliakcay.malinesscore.gui.MenuService;
import com.mertaliakcay.malinesscore.gui.model.MenuClickType;
import com.mertaliakcay.malinesscore.gui.model.MenuSession;
import com.mertaliakcay.malinesscore.gui.model.ReleaseSource;
import com.mertaliakcay.malinesscore.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class DemoMandatoryAfkProvider implements MenuContentProvider {

    private static final List<Material> DECOY_MATERIALS = List.of(
            Material.STONE,
            Material.DIRT,
            Material.OAK_LOG,
            Material.COBBLESTONE,
            Material.SAND,
            Material.GRAVEL,
            Material.COAL,
            Material.IRON_INGOT
    );

    private final MenuService menuService;

    public DemoMandatoryAfkProvider(MenuService menuService) {
        this.menuService = menuService;
    }

    @Override
    public String getId() {
        return "demo-mandatory-afk";
    }

    @Override
    public void initialize(MenuSession session) {
        List<Integer> slots = new ArrayList<>(List.of(0, 1, 2, 3, 4));
        Collections.shuffle(slots);

        Map<Integer, ItemStack> bySlot = new HashMap<>();
        Map<Integer, String> entryBySlot = new HashMap<>();

        int melonSlot = slots.get(0);
        bySlot.put(melonSlot, createMelon());
        entryBySlot.put(melonSlot, "melon");

        for (int index = 1; index < slots.size(); index++) {
            int slot = slots.get(index);
            bySlot.put(slot, createDecoy(index));
            entryBySlot.put(slot, "decoy-" + index);
        }

        session.getProviderState().put("afk-by-slot", bySlot);
        session.getProviderState().put("afk-entry-by-slot", entryBySlot);
    }

    @Override
    public int getTotalPages(MenuSession session) {
        return 1;
    }

    @Override
    public void populatePage(MenuSession session) {
        @SuppressWarnings("unchecked")
        Map<Integer, ItemStack> bySlot = (Map<Integer, ItemStack>) session.getProviderState().get("afk-by-slot");
        @SuppressWarnings("unchecked")
        Map<Integer, String> entryBySlot = (Map<Integer, String>) session.getProviderState().get("afk-entry-by-slot");

        if (bySlot == null || entryBySlot == null) {
            return;
        }

        List<ItemStack> stacks = new ArrayList<>();
        List<String> entryIds = new ArrayList<>();
        List<Integer> slots = new ArrayList<>(session.getDefinition().getContentSlots());
        if (slots.isEmpty()) {
            slots = List.of(0, 1, 2, 3, 4);
        }

        for (int slot : slots) {
            ItemStack stack = bySlot.get(slot);
            if (stack != null) {
                stacks.add(stack.clone());
                entryIds.add(entryBySlot.get(slot));
            } else {
                stacks.add(null);
                entryIds.add("");
            }
        }

        session.getProviderState().put("page-items", stacks);
        session.getProviderState().put("page-entry-ids", entryIds);
        session.getProviderState().put("content-slots-override", slots);
    }

    @Override
    public void onContentClick(Player player, MenuSession session, String entryId, MenuClickType clickType) {
        if ("melon".equals(entryId) && clickType == MenuClickType.LEFT) {
            menuService.release(player, ReleaseSource.SYSTEM, "melon-selected");
            return;
        }
        player.sendMessage(ColorUtil.colorize("&cYanlış seçim! Karpuzu seçmelisin."));
    }

    private ItemStack createMelon() {
        ItemStack stack = new ItemStack(Material.MELON_SLICE);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorUtil.colorizePlain("&aKarpuz"));
            meta.setLore(List.of(ColorUtil.colorizePlain("&7AFK değilsen bunu seç!")));
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private ItemStack createDecoy(int seed) {
        Material material = DECOY_MATERIALS.get(ThreadLocalRandom.current().nextInt(DECOY_MATERIALS.size()));
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorUtil.colorizePlain("&7Rastgele #" + seed));
            stack.setItemMeta(meta);
        }
        return stack;
    }
}
