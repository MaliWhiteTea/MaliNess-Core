package com.mertaliakcay.malinesscore.systems.home;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class HomeRateLimiter {

    private final int maxFailures;
    private final long windowMillis;
    private final ConcurrentMap<java.util.UUID, FailureWindow> failures = new ConcurrentHashMap<>();

    public HomeRateLimiter(int maxFailures, long windowMillis) {
        this.maxFailures = maxFailures;
        this.windowMillis = windowMillis;
    }

    public boolean isRateLimited(Player player) {
        FailureWindow window = failures.get(player.getUniqueId());
        if (window == null) {
            return false;
        }

        window.cleanup(windowMillis);
        return window.count >= maxFailures;
    }

    public void recordFailure(Player player) {
        if (player == null) {
            return;
        }
        failures.compute(player.getUniqueId(), (id, window) -> {
            if (window == null) {
                window = new FailureWindow();
            }
            window.cleanup(windowMillis);
            window.count++;
            window.lastFailure = System.currentTimeMillis();
            return window;
        });
    }

    public void reset(Player player) {
        failures.remove(player.getUniqueId());
    }

    private static final class FailureWindow {
        private int count;
        private long lastFailure;

        private void cleanup(long windowMillis) {
            if (System.currentTimeMillis() - lastFailure > windowMillis) {
                count = 0;
            }
        }
    }
}
