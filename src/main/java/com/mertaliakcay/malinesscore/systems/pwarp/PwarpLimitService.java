package com.mertaliakcay.malinesscore.systems.pwarp;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public final class PwarpLimitService {

    private final int defaultMaxPwarps;
    private final int maxPermissionScan;

    public PwarpLimitService(int defaultMaxPwarps, int maxPermissionScan) {
        this.defaultMaxPwarps = defaultMaxPwarps;
        this.maxPermissionScan = maxPermissionScan;
    }

    public int getMaxPwarps(Player player) {
        int max = defaultMaxPwarps;

        for (int count = 1; count <= maxPermissionScan; count++) {
            if (player.hasPermission("maliness-core.pwarp.count." + count)) {
                max = Math.max(max, count);
            }
        }

        return max;
    }

    public int getMaxPwarps(OfflinePlayer player) {
        Player online = player.getPlayer();
        if (online != null) {
            return getMaxPwarps(online);
        }

        return defaultMaxPwarps;
    }

    public boolean canCreate(Player player, int currentCount) {
        return currentCount < getMaxPwarps(player);
    }

    public boolean isOverLimit(Player player, int currentCount) {
        return currentCount > getMaxPwarps(player);
    }
}
