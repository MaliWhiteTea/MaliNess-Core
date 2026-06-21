package com.mertaliakcay.malinesscore.teleport;

import com.mertaliakcay.malinesscore.util.SystemLang;
import org.bukkit.entity.Player;

public final class TeleportWarmupMessages {

    private TeleportWarmupMessages() {
    }

    public static void sendBlocked(Player player, SystemLang lang, WarmupType active, WarmupType self) {
        if (active == self) {
            lang.send(player, "teleport-already-pending");
            return;
        }

        switch (active) {
            case HOME -> lang.send(player, "teleport-blocked-by-home");
            case WARP -> lang.send(player, "teleport-blocked-by-warp");
            case PWARP -> lang.send(player, "teleport-blocked-by-pwarp");
        }
    }
}
