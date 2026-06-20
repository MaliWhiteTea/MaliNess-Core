package com.mertaliakcay.malinesscore.systems.vanish;

import org.bukkit.Location;

import java.util.UUID;

public interface VanishPacketEnhancer {

    void enable();

    void disable();

    void registerPlayer(org.bukkit.entity.Player player);

    void unregisterPlayer(org.bukkit.entity.Player player);

    void trackInteraction(UUID playerId, Location location);
}
