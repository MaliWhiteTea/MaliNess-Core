package com.mertaliakcay.malinesscore.systems.playtime;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

final class PlaytimeTracker {

    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();

    void startSession(UUID playerId) {
        sessions.putIfAbsent(playerId, new Session(System.currentTimeMillis()));
    }

    void endSession(UUID playerId) {
        sessions.remove(playerId);
    }

    long collectSessionMillis(UUID playerId) {
        Session session = sessions.get(playerId);
        if (session == null) {
            return 0L;
        }
        return session.collectActiveMillis();
    }

    void pauseSession(UUID playerId) {
        Session session = sessions.get(playerId);
        if (session != null) {
            session.pause();
        }
    }

    void resumeSession(UUID playerId) {
        Session session = sessions.get(playerId);
        if (session != null) {
            session.resume();
        }
    }

    boolean hasSession(UUID playerId) {
        return sessions.containsKey(playerId);
    }

    private static final class Session {
        private long activeStartMillis;
        private long accumulatedMillis;
        private boolean paused;

        private Session(long startedAtMillis) {
            this.activeStartMillis = startedAtMillis;
        }

        private void pause() {
            if (paused) {
                return;
            }

            accumulatedMillis += Math.max(0L, System.currentTimeMillis() - activeStartMillis);
            paused = true;
        }

        private void resume() {
            if (!paused) {
                return;
            }

            activeStartMillis = System.currentTimeMillis();
            paused = false;
        }

        private long collectActiveMillis() {
            if (paused) {
                return accumulatedMillis;
            }

            return accumulatedMillis + Math.max(0L, System.currentTimeMillis() - activeStartMillis);
        }
    }
}
