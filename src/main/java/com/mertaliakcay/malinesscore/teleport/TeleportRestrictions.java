package com.mertaliakcay.malinesscore.teleport;

import com.mertaliakcay.malinesscore.systems.home.HomeSystem;
import com.mertaliakcay.malinesscore.systems.warp.WarpSystem;
import org.bukkit.entity.Player;

public final class TeleportRestrictions {

    private TeleportRestrictions() {
    }

    public static boolean bypassesVehicleRestrictions(Player player) {
        return HomeSystem.bypassesHomeRestrictions(player)
                || WarpSystem.bypassesWarpRestrictions(player);
    }
}
