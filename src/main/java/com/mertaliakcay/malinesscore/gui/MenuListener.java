package com.mertaliakcay.malinesscore.gui;

import com.mertaliakcay.malinesscore.MaliNessCore;
import com.mertaliakcay.malinesscore.gui.holder.MaliNessMenuHolder;
import com.mertaliakcay.malinesscore.gui.model.ClosePolicy;
import com.mertaliakcay.malinesscore.gui.model.MenuClickProtectionSettings;
import com.mertaliakcay.malinesscore.gui.model.MenuClickType;
import com.mertaliakcay.malinesscore.gui.model.MenuItemDefinition;
import com.mertaliakcay.malinesscore.gui.model.MenuSession;
import com.mertaliakcay.malinesscore.gui.model.PlayerInventoryPolicy;
import com.mertaliakcay.malinesscore.util.SystemLang;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MenuListener implements Listener {

    private final MaliNessCore plugin;
    private final MenuService menuService;
    private final MenuRegistry menuRegistry;
    private final MenuActionExecutor actionExecutor;
    private final SystemLang lang;
    private final Map<UUID, Long> lastClickAt = new ConcurrentHashMap<>();
    private final Map<String, Long> lastSlotClickAt = new ConcurrentHashMap<>();

    public MenuListener(
            MaliNessCore plugin,
            MenuService menuService,
            MenuRegistry menuRegistry,
            MenuActionExecutor actionExecutor,
            SystemLang lang
    ) {
        this.plugin = plugin;
        this.menuService = menuService;
        this.menuRegistry = menuRegistry;
        this.actionExecutor = actionExecutor;
        this.lang = lang;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        MenuSession session = menuService.getSession(player);
        if (session == null) {
            return;
        }

        InventoryView view = event.getView();
        if (view.getTopInventory().getHolder() != session.getHolder()) {
            return;
        }

        int topSize = view.getTopInventory().getSize();
        int rawSlot = event.getRawSlot();
        boolean clickedTop = rawSlot >= 0 && rawSlot < topSize;

        if (event.isShiftClick()) {
            event.setCancelled(true);
        } else if (!clickedTop) {
            if (session.getDefinition().getPlayerInventoryPolicy() == PlayerInventoryPolicy.LOCKED) {
                event.setCancelled(true);
            }
            return;
        } else {
            event.setCancelled(true);
        }

        if (!clickedTop) {
            return;
        }

        if (shouldIgnoreRapidClick(player, rawSlot)) {
            return;
        }

        MenuClickType clickType = MenuClickType.fromBukkit(event.getClick(), event.getHotbarButton()).orElse(null);
        if (clickType == null) {
            return;
        }

        int slot = event.getRawSlot();
        String contentEntry = session.getContentEntryBySlot().get(slot);
        if (contentEntry != null && !contentEntry.isBlank()) {
            actionExecutor.executeContentClick(player, session, contentEntry, clickType);
            return;
        }

        for (MenuItemDefinition item : session.getDefinition().getItems().values()) {
            if (item.getSlot() == slot && item.isVisible(session)) {
                actionExecutor.executeChromeClick(player, session, item, clickType);
                return;
            }
        }
    }

    private boolean shouldIgnoreRapidClick(Player player, int slot) {
        MenuClickProtectionSettings protection = menuRegistry.getClickProtection();
        long now = System.currentTimeMillis();

        if (protection.isDoubleClickGuard()) {
            Long previous = lastClickAt.get(player.getUniqueId());
            if (previous != null && now - previous < 100L) {
                return true;
            }
            lastClickAt.put(player.getUniqueId(), now);
        }

        if (protection.isButtonCooldownEnabled()) {
            String key = player.getUniqueId() + ":" + slot;
            Long previousSlot = lastSlotClickAt.get(key);
            if (previousSlot != null && now - previousSlot < protection.getButtonCooldownMillis()) {
                return true;
            }
            lastSlotClickAt.put(key, now);
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        MenuSession session = menuService.getSession(player);
        if (session == null || event.getView().getTopInventory().getHolder() != session.getHolder()) {
            return;
        }

        int topSize = event.getView().getTopInventory().getSize();
        boolean touchesTop = event.getRawSlots().stream().anyMatch(slot -> slot < topSize);
        if (touchesTop) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        MenuSession session = menuService.getSession(player);
        if (session == null || event.getInventory().getHolder() != session.getHolder()) {
            return;
        }

        if (event.getInventory() != session.getInventory()) {
            return;
        }

        if (session.isClosingIntentionally()) {
            return;
        }

        if (session.getDefinition().getClosePolicy() == ClosePolicy.MANDATORY) {
            if (player.isDead()
                    || menuService.getMandatorySessionStore().hasPendingDeathRespawn(player.getUniqueId())) {
                return;
            }
            Bukkit.getScheduler().runTask(plugin, () -> menuService.reopenMandatory(player));
            return;
        }

        menuService.discardSession(player);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        MenuSession session = menuService.getSession(player);
        if (!menuService.isMandatoryMenuOpen(player)) {
            if (session == null) {
                return;
            }
            if (!menuService.getRegistry().isCloseOnOtherInventory()) {
                return;
            }
            Inventory opened = event.getInventory();
            if (opened.getHolder() instanceof MaliNessMenuHolder) {
                return;
            }
            menuService.close(player, true);
            return;
        }

        Inventory opened = event.getInventory();
        if (!(opened.getHolder() instanceof MaliNessMenuHolder holder
                && holder.getPlayerId().equals(player.getUniqueId()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!menuService.isMandatoryMenuOpen(player)) {
            return;
        }
        MenuSession session = menuService.getSession(player);
        if (session == null || !session.getDefinition().isBlockMovement()) {
            return;
        }
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAsyncChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!menuService.isMandatoryMenuOpen(player)) {
            return;
        }
        MenuSession session = menuService.getSession(player);
        if (session == null || session.getDefinition().isAllowChat()) {
            return;
        }
        event.setCancelled(true);
        Bukkit.getScheduler().runTask(plugin, () -> lang.send(player, "mandatory-chat-blocked"));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        MenuSession session = menuService.getSession(player);
        if (session == null) {
            return;
        }

        if (menuService.isMandatoryMenuOpen(player)) {
            event.setCancelled(true);
            lang.send(player, "mandatory-command-blocked");
            return;
        }

        if (session.getDefinition().isCloseOnCommand()) {
            menuService.close(player, true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        MenuSession session = menuService.getSession(player);
        if (session == null) {
            return;
        }

        if (!menuService.getRegistry().isPickupWhileMenuOpen()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        menuService.onPlayerQuit(event.getPlayer());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        menuService.onPlayerJoin(event.getPlayer());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        menuService.onPlayerDeath(event.getEntity());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        menuService.onPlayerRespawn(event.getPlayer());
    }
}
