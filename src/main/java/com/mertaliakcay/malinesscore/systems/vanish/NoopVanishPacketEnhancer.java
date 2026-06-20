package com.mertaliakcay.malinesscore.systems.vanish;

public final class NoopVanishPacketEnhancer implements VanishPacketEnhancer {

    @Override
    public void enable() {
    }

    @Override
    public void disable() {
    }

    @Override
    public void registerPlayer(org.bukkit.entity.Player player) {
    }

    @Override
    public void unregisterPlayer(org.bukkit.entity.Player player) {
    }

    @Override
    public void trackInteraction(java.util.UUID playerId, org.bukkit.Location location) {
    }
}
