package com.mertaliakcay.malinesscore.systems.vanish;

import org.bukkit.Location;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

final class VanishInteractionTracker {

    private static final long INTERACTION_TTL_MS = 3_000L;

    private final Map<UUID, TrackedInteraction> interactions = new ConcurrentHashMap<>();

    void track(UUID playerId, Location location) {
        if (location == null || location.getWorld() == null) {
            return;
        }

        interactions.put(playerId, new TrackedInteraction(
                playerId,
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ(),
                location.getWorld().getName(),
                System.currentTimeMillis() + INTERACTION_TTL_MS
        ));
    }

    void clear(UUID playerId) {
        interactions.remove(playerId);
    }

    void clearAll() {
        interactions.clear();
    }

    UUID findOwnerNear(int x, int y, int z, String worldName) {
        cleanupExpired();

        for (TrackedInteraction interaction : interactions.values()) {
            if (!interaction.worldName().equals(worldName)) {
                continue;
            }

            if (Math.abs(interaction.blockX() - x) <= 1
                    && Math.abs(interaction.blockY() - y) <= 1
                    && Math.abs(interaction.blockZ() - z) <= 1) {
                return interaction.playerId();
            }
        }

        return null;
    }

    private void cleanupExpired() {
        Iterator<Map.Entry<UUID, TrackedInteraction>> iterator = interactions.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue().isExpired()) {
                iterator.remove();
            }
        }
    }

    private record TrackedInteraction(
            UUID playerId,
            int blockX,
            int blockY,
            int blockZ,
            String worldName,
            long expiresAtMillis
    ) {
        private boolean isExpired() {
            return System.currentTimeMillis() >= expiresAtMillis;
        }
    }
}
