package com.mertaliakcay.malinesscore.gui.holder;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

public final class MaliNessMenuHolder implements InventoryHolder {

    private final String menuId;
    private final UUID playerId;
    private Inventory inventory;

    public MaliNessMenuHolder(String menuId, UUID playerId) {
        this.menuId = menuId;
        this.playerId = playerId;
    }

    public String getMenuId() {
        return menuId;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public void bind(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
