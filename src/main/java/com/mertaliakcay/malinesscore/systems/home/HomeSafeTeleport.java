package com.mertaliakcay.malinesscore.systems.home;

import com.mertaliakcay.malinesscore.teleport.SafeTeleport;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class HomeSafeTeleport {

    private HomeSafeTeleport() {
    }

    public static boolean isSafe(Location location) {
        return SafeTeleport.isSafe(location);
    }

    public static boolean isValidSetLocation(Player player, Location location) {
        return SafeTeleport.isValidSetLocation(player, location);
    }
}
