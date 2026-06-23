package com.mertaliakcay.malinesscore.gui;

import com.mertaliakcay.malinesscore.MaliNessCore;
import com.mertaliakcay.malinesscore.gui.content.MenuContentProvider;
import com.mertaliakcay.malinesscore.gui.holder.MaliNessMenuHolder;
import com.mertaliakcay.malinesscore.gui.model.ClosePolicy;
import com.mertaliakcay.malinesscore.gui.model.MenuDefinition;
import com.mertaliakcay.malinesscore.gui.model.MenuSession;
import com.mertaliakcay.malinesscore.gui.model.ReleaseSource;
import com.mertaliakcay.malinesscore.gui.util.MenuItemBuilder;
import com.mertaliakcay.malinesscore.util.ColorUtil;
import com.mertaliakcay.malinesscore.util.SystemLang;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class MenuService {

    private final MaliNessCore plugin;
    private final MenuRegistry registry;
    private final MenuRenderer renderer;
    private final MandatorySessionStore mandatorySessionStore;
    private final SystemLang lang;
    private MenuActionExecutor actionExecutor;
    private final Logger logger;
    private final Map<UUID, MenuSession> sessions = new ConcurrentHashMap<>();
    private List<MandatorySessionStore.Snapshot> reloadSnapshots = List.of();

    public MenuService(
            MaliNessCore plugin,
            MenuRegistry registry,
            MenuRenderer renderer,
            MandatorySessionStore mandatorySessionStore,
            SystemLang lang
    ) {
        this.plugin = plugin;
        this.registry = registry;
        this.renderer = renderer;
        this.mandatorySessionStore = mandatorySessionStore;
        this.lang = lang;
        this.logger = plugin.getLogger();
    }

    public void setActionExecutor(MenuActionExecutor actionExecutor) {
        this.actionExecutor = actionExecutor;
    }

    public MenuActionExecutor getActionExecutor() {
        return actionExecutor;
    }

    public MenuRegistry getRegistry() {
        return registry;
    }

    public MandatorySessionStore getMandatorySessionStore() {
        return mandatorySessionStore;
    }

    public boolean open(Player player, String menuId) {
        return openInternal(player, menuId, null);
    }

    private boolean openInternal(Player player, String menuId, MandatorySessionStore.Snapshot snapshot) {
        if (!registry.isEnabled()) {
            lang.send(player, "menu-disabled");
            return false;
        }

        MenuDefinition definition = registry.getMenu(menuId);
        if (definition == null) {
            lang.send(player, "menu-not-found", "id", menuId);
            return false;
        }

        if (!player.hasPermission(definition.getPermission())) {
            lang.send(player, "no-permission");
            return false;
        }

        close(player, true);

        MaliNessMenuHolder holder = new MaliNessMenuHolder(menuId, player.getUniqueId());
        MenuSession session = new MenuSession(player.getUniqueId(), definition, holder);

        MenuContentProvider provider = registry.getContentProvider(definition.getContentProviderId());
        if (provider != null) {
            if (snapshot != null && !snapshot.providerState().isEmpty()) {
                session.getProviderState().putAll(snapshot.providerState());
            } else {
                provider.initialize(session);
            }
            session.setMaxPage(provider.getTotalPages(session));
        }

        if (snapshot != null) {
            session.setCurrentPage(snapshot.currentPage());
            session.setSortMode(snapshot.sortMode());
            session.setFilterMode(snapshot.filterMode());
        }

        String titleText = MenuItemBuilder.applyPlaceholdersPlain(
                definition.getTitle(),
                session,
                player,
                plugin
        );
        Inventory inventory = createInventory(holder, definition, ColorUtil.colorize(titleText));
        session.setInventory(inventory);

        renderer.render(session);
        sessions.put(player.getUniqueId(), session);
        player.openInventory(inventory);
        return true;
    }

    public boolean openFromSnapshot(Player player, MandatorySessionStore.Snapshot snapshot) {
        if (snapshot == null) {
            return false;
        }
        return openInternal(player, snapshot.menuId(), snapshot);
    }

    public void discardSession(Player player) {
        sessions.remove(player.getUniqueId());
    }

    public void close(Player player, boolean force) {
        MenuSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            return;
        }

        if (!force && session.getDefinition().getClosePolicy() == ClosePolicy.MANDATORY) {
            return;
        }

        session.setClosingIntentionally(true);
        sessions.remove(player.getUniqueId());
        player.closeInventory();
    }

    public boolean release(Player player, ReleaseSource source, String detail) {
        MenuSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            mandatorySessionStore.clear(player.getUniqueId());
            return false;
        }

        if (session.getDefinition().getClosePolicy() != ClosePolicy.MANDATORY) {
            close(player, true);
            return false;
        }

        if (registry.isMandatoryReleaseLogging()) {
            logger.info("[GUI] mandatory-release player=" + player.getName()
                    + " menu=" + session.getDefinition().getId()
                    + " source=" + source.name()
                    + (detail != null && !detail.isBlank() ? " detail=" + detail : ""));
        }

        mandatorySessionStore.clear(player.getUniqueId());
        close(player, true);
        return true;
    }

    public MenuSession getSession(Player player) {
        return sessions.get(player.getUniqueId());
    }

    public MenuSession getSession(UUID playerId) {
        return sessions.get(playerId);
    }

    public boolean hasSession(Player player) {
        return sessions.containsKey(player.getUniqueId());
    }

    public boolean hasMandatorySession(Player player) {
        return isMandatoryMenuOpen(player);
    }

    public boolean isMandatoryMenuOpen(Player player) {
        MenuSession session = getSession(player);
        if (session == null || session.getDefinition().getClosePolicy() != ClosePolicy.MANDATORY) {
            return false;
        }
        return player.getOpenInventory().getTopInventory().getHolder() == session.getHolder();
    }

    public void reopenMandatory(Player player) {
        if (mandatorySessionStore.hasPendingDeathRespawn(player.getUniqueId())) {
            return;
        }
        MenuSession session = sessions.get(player.getUniqueId());
        if (session == null || session.getDefinition().getClosePolicy() != ClosePolicy.MANDATORY) {
            return;
        }
        if (player.isOnline() && player.getOpenInventory().getTopInventory().getHolder() == session.getHolder()) {
            return;
        }
        session.setClosingIntentionally(false);
        renderer.render(session);
        player.openInventory(session.getInventory());
    }

    public void refreshView(Player player, MenuSession session) {
        String titleText = MenuItemBuilder.applyPlaceholdersPlain(
                session.getDefinition().getTitle(),
                session,
                player,
                plugin
        );
        Inventory inventory = createInventory(session.getHolder(), session.getDefinition(), ColorUtil.colorize(titleText));
        session.setInventory(inventory);
        renderer.render(session);
        player.openInventory(inventory);
    }

    public void prepareReload() {
        List<MandatorySessionStore.Snapshot> snapshots = new ArrayList<>();
        for (Map.Entry<UUID, MenuSession> entry : sessions.entrySet()) {
            MenuSession session = entry.getValue();
            if (session.getDefinition().getClosePolicy() == ClosePolicy.MANDATORY) {
                snapshots.add(new MandatorySessionStore.Snapshot(
                        entry.getKey(),
                        session.getDefinition().getId(),
                        session.getCurrentPage(),
                        session.getSortMode(),
                        session.getFilterMode(),
                        new java.util.HashMap<>(session.getProviderState())
                ));
            }
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null) {
                session.setClosingIntentionally(true);
                player.closeInventory();
            }
        }
        sessions.clear();
        reloadSnapshots = snapshots;
    }

    public void finishReload() {
        List<MandatorySessionStore.Snapshot> snapshots = reloadSnapshots;
        reloadSnapshots = List.of();
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (MandatorySessionStore.Snapshot snapshot : snapshots) {
                Player player = Bukkit.getPlayer(snapshot.playerId());
                if (player != null && player.isOnline()) {
                    openFromSnapshot(player, snapshot);
                }
            }
        });
    }

    public void onPlayerQuit(Player player) {
        MenuSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            return;
        }

        if (session.getDefinition().getClosePolicy() == ClosePolicy.MANDATORY
                && !session.getDefinition().isEscapeOnQuit()) {
            mandatorySessionStore.saveRejoin(player.getUniqueId(), session);
        } else {
            mandatorySessionStore.clear(player.getUniqueId());
        }

        session.setClosingIntentionally(true);
        sessions.remove(player.getUniqueId());
    }

    public void onPlayerJoin(Player player) {
        MandatorySessionStore.Snapshot snapshot = mandatorySessionStore.consumeRejoin(player.getUniqueId());
        if (snapshot != null) {
            Bukkit.getScheduler().runTask(plugin, () -> openFromSnapshot(player, snapshot));
        }
    }

    public void onPlayerRespawn(Player player) {
        MandatorySessionStore.Snapshot snapshot = mandatorySessionStore.consumeDeathRespawn(player.getUniqueId());
        if (snapshot != null) {
            Bukkit.getScheduler().runTask(plugin, () -> openFromSnapshot(player, snapshot));
        }
    }

    public void onPlayerDeath(Player player) {
        MenuSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            return;
        }

        session.setClosingIntentionally(true);
        sessions.remove(player.getUniqueId());

        if (session.getDefinition().getClosePolicy() == ClosePolicy.MANDATORY
                && !session.getDefinition().isEscapeOnDeath()) {
            mandatorySessionStore.saveDeathRespawn(player.getUniqueId(), session);
            return;
        }

        mandatorySessionStore.clear(player.getUniqueId());
    }

    public void shutdown() {
        for (UUID playerId : List.copyOf(sessions.keySet())) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                close(player, true);
            }
        }
        sessions.clear();
        mandatorySessionStore.clearAll();
    }

    private Inventory createInventory(MaliNessMenuHolder holder, MenuDefinition definition, Component title) {
        if (definition.getInventoryType() == InventoryType.CHEST) {
            return Bukkit.createInventory(holder, definition.getChestSize(), title);
        }
        return Bukkit.createInventory(holder, definition.getInventoryType(), title);
    }
}
