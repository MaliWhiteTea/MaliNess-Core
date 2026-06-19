package com.mertaliakcay.malinesscore.systems.control;

import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public final class SystemPermissions {

    public static final String PERM_LIST = "maliness-core.systems.list";
    public static final String PERM_MANAGE_PREFIX = "maliness-core.systems.manage.";
    public static final String PERM_MANAGE_ALL = "maliness-core.systems.manage.*";

    private SystemPermissions() {
    }

    public static String managePermission(String systemId) {
        return PERM_MANAGE_PREFIX + systemId;
    }

    public static boolean canManage(CommandSender sender, String systemId) {
        return sender.hasPermission(managePermission(systemId))
                || sender.hasPermission(PERM_MANAGE_ALL);
    }

    public static boolean canList(CommandSender sender, Iterable<String> gameSystemIds) {
        if (sender.hasPermission(PERM_LIST)) {
            return true;
        }

        for (String systemId : gameSystemIds) {
            if (canManage(sender, systemId)) {
                return true;
            }
        }

        return false;
    }

    public static Permission createManagePermission(String systemId) {
        return new Permission(
                managePermission(systemId),
                "Belirli bir oyun sistemini açar veya kapatır.",
                PermissionDefault.OP
        );
    }
}
