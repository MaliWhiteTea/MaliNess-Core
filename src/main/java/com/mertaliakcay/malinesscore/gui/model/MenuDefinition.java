package com.mertaliakcay.malinesscore.gui.model;

import org.bukkit.event.inventory.InventoryType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MenuDefinition {

    private final String id;
    private final InventoryType inventoryType;
    private final int chestSize;
    private final String title;
    private final String permission;
    private final PlayerInventoryPolicy playerInventoryPolicy;
    private final ClosePolicy closePolicy;
    private final boolean closeOnCommand;
    private final boolean escapeOnDeath;
    private final boolean escapeOnQuit;
    private final boolean blockMovement;
    private final boolean allowDamage;
    private final boolean allowChat;
    private final boolean fillerEnabled;
    private final String fillerMaterial;
    private final String fillerName;
    private final List<Integer> contentSlots;
    private final String contentProviderId;
    private final Map<String, MenuItemDefinition> items;
    private final EconomyMenuBehavior economyBehavior;

    public MenuDefinition(
            String id,
            InventoryType inventoryType,
            int chestSize,
            String title,
            String permission,
            PlayerInventoryPolicy playerInventoryPolicy,
            ClosePolicy closePolicy,
            boolean closeOnCommand,
            boolean escapeOnDeath,
            boolean escapeOnQuit,
            boolean blockMovement,
            boolean allowDamage,
            boolean allowChat,
            boolean fillerEnabled,
            String fillerMaterial,
            String fillerName,
            List<Integer> contentSlots,
            String contentProviderId,
            Map<String, MenuItemDefinition> items,
            EconomyMenuBehavior economyBehavior
    ) {
        this.id = id;
        this.inventoryType = inventoryType;
        this.chestSize = chestSize;
        this.title = title;
        this.permission = permission;
        this.playerInventoryPolicy = playerInventoryPolicy;
        this.closePolicy = closePolicy;
        this.closeOnCommand = closeOnCommand;
        this.escapeOnDeath = escapeOnDeath;
        this.escapeOnQuit = escapeOnQuit;
        this.blockMovement = blockMovement;
        this.allowDamage = allowDamage;
        this.allowChat = allowChat;
        this.fillerEnabled = fillerEnabled;
        this.fillerMaterial = fillerMaterial;
        this.fillerName = fillerName;
        this.contentSlots = List.copyOf(contentSlots);
        this.contentProviderId = contentProviderId;
        this.items = Map.copyOf(items);
        this.economyBehavior = economyBehavior;
    }

    public String getId() {
        return id;
    }

    public InventoryType getInventoryType() {
        return inventoryType;
    }

    public int getChestSize() {
        return chestSize;
    }

    public int getTopSize() {
        if (inventoryType == InventoryType.CHEST) {
            return chestSize;
        }
        return inventoryType.getDefaultSize();
    }

    public String getTitle() {
        return title;
    }

    public String getPermission() {
        return permission;
    }

    public PlayerInventoryPolicy getPlayerInventoryPolicy() {
        return playerInventoryPolicy;
    }

    public ClosePolicy getClosePolicy() {
        return closePolicy;
    }

    public boolean isCloseOnCommand() {
        return closeOnCommand;
    }

    public boolean isEscapeOnDeath() {
        return escapeOnDeath;
    }

    public boolean isEscapeOnQuit() {
        return escapeOnQuit;
    }

    public boolean isBlockMovement() {
        return blockMovement;
    }

    public boolean isAllowDamage() {
        return allowDamage;
    }

    public boolean isAllowChat() {
        return allowChat;
    }

    public boolean isFillerEnabled() {
        return fillerEnabled;
    }

    public String getFillerMaterial() {
        return fillerMaterial;
    }

    public String getFillerName() {
        return fillerName;
    }

    public List<Integer> getContentSlots() {
        return contentSlots;
    }

    public String getContentProviderId() {
        return contentProviderId;
    }

    public Map<String, MenuItemDefinition> getItems() {
        return items;
    }

    public EconomyMenuBehavior getEconomyBehavior() {
        return economyBehavior;
    }
}
