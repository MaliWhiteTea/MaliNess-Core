package com.mertaliakcay.malinesscore.util;

import org.bukkit.command.CommandSender;

public final class CommandSuggestGate {

    private CommandSuggestGate() {
    }

    public static boolean hasAny(CommandSender sender, String... permissions) {
        for (String permission : permissions) {
            if (sender.hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }
}
