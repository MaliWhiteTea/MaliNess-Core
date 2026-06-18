package com.mertaliakcay.malinesscore.systems.home;

import com.mertaliakcay.malinesscore.systems.home.model.PlayerHomes;
import org.bukkit.entity.Player;

public final class HomeLimitService {

    private final int defaultMaxHomes;
    private final int maxPermissionScan;

    public HomeLimitService(int defaultMaxHomes, int maxPermissionScan) {
        this.defaultMaxHomes = defaultMaxHomes;
        this.maxPermissionScan = maxPermissionScan;
    }

    public int getMaxHomes(Player player) {
        int max = defaultMaxHomes;

        for (int count = 1; count <= maxPermissionScan; count++) {
            if (player.hasPermission("maliness-core.home.count." + count)) {
                max = Math.max(max, count);
            }
        }

        return max;
    }

    public boolean isOverLimit(Player player, PlayerHomes homes) {
        return homes.size() > getMaxHomes(player);
    }

    public boolean canCreateHome(Player player, PlayerHomes homes) {
        return homes.size() < getMaxHomes(player);
    }
}
