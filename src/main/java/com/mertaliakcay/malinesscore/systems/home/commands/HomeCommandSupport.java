package com.mertaliakcay.malinesscore.systems.home.commands;

import com.mertaliakcay.malinesscore.systems.home.HomeService;
import com.mertaliakcay.malinesscore.systems.home.HomeSystem;
import org.bukkit.command.CommandSender;

final class HomeCommandSupport {

    private HomeCommandSupport() {
    }

    static HomeService requireService(HomeSystem system, CommandSender sender) {
        if (!system.isActive()) {
            system.getLang().send(sender, "system-disabled");
            return null;
        }

        HomeService service = system.getHomeService();
        if (service == null) {
            system.getLang().send(sender, "system-disabled");
        }
        return service;
    }

    static boolean canSuggestSetHome(CommandSender sender) {
        return sender.hasPermission(HomeSystem.PERM_SETHOME);
    }

    static boolean canSuggestHome(CommandSender sender) {
        return sender.hasPermission(HomeSystem.PERM_USE)
                || sender.hasPermission(HomeSystem.PERM_OTHERS_TELEPORT);
    }

    static boolean canSuggestDelHome(CommandSender sender) {
        return sender.hasPermission(HomeSystem.PERM_DELHOME)
                || sender.hasPermission(HomeSystem.PERM_OTHERS_DELETE);
    }

    static boolean canSuggestHomes(CommandSender sender) {
        return sender.hasPermission(HomeSystem.PERM_HOMES)
                || sender.hasPermission(HomeSystem.PERM_OTHERS_LIST);
    }

    static boolean canSuggestRenameHome(CommandSender sender) {
        return sender instanceof org.bukkit.entity.Player
                && sender.hasPermission(HomeSystem.PERM_RENAME);
    }
}
