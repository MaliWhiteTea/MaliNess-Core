package com.mertaliakcay.malinesscore.gui;

import com.mertaliakcay.malinesscore.gui.model.MenuSession;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class MandatorySessionStore {

    public record Snapshot(
            UUID playerId,
            String menuId,
            int currentPage,
            MenuSession.SortMode sortMode,
            MenuSession.FilterMode filterMode,
            Map<String, Object> providerState
    ) {
    }

    private final Map<UUID, Snapshot> pendingRejoin = new HashMap<>();
    private final Map<UUID, Snapshot> pendingDeathRespawn = new HashMap<>();

    public void saveRejoin(UUID playerId, MenuSession session) {
        pendingRejoin.put(playerId, snapshotFrom(session));
    }

    public Snapshot consumeRejoin(UUID playerId) {
        return pendingRejoin.remove(playerId);
    }

    public void saveDeathRespawn(UUID playerId, MenuSession session) {
        pendingDeathRespawn.put(playerId, snapshotFrom(session));
    }

    public Snapshot consumeDeathRespawn(UUID playerId) {
        return pendingDeathRespawn.remove(playerId);
    }

    public boolean hasPendingDeathRespawn(UUID playerId) {
        return pendingDeathRespawn.containsKey(playerId);
    }

    public void clear(UUID playerId) {
        pendingRejoin.remove(playerId);
        pendingDeathRespawn.remove(playerId);
    }

    public void clearAll() {
        pendingRejoin.clear();
        pendingDeathRespawn.clear();
    }

    private Snapshot snapshotFrom(MenuSession session) {
        return new Snapshot(
                session.getPlayerId(),
                session.getDefinition().getId(),
                session.getCurrentPage(),
                session.getSortMode(),
                session.getFilterMode(),
                new HashMap<>(session.getProviderState())
        );
    }
}
