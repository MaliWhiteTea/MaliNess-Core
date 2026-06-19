package com.mertaliakcay.malinesscore.systems.home;

import com.mertaliakcay.malinesscore.util.CommandSuggestions;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public final class HomeMnCommand {

    private final HomeService service;

    public HomeMnCommand(HomeService service) {
        this.service = service;
    }

    public void handle(CommandSender sender, String subcommand, String[] args) {
        if (isSetHome(subcommand)) {
            service.handleSetHome(sender, args);
        } else if (isHome(subcommand)) {
            service.handleHome(sender, args);
        } else if (isDelHome(subcommand)) {
            service.handleDelHome(sender, args);
        } else if (isHomes(subcommand)) {
            service.handleHomes(sender, args);
        } else if (isRenameHome(subcommand)) {
            service.handleRenameHome(sender, args);
        }
    }

    public List<String> onTabComplete(CommandSender sender, String subcommand, String[] args) {
        if (isSetHome(subcommand)) {
            return service.suggestSetHome(sender, args);
        }
        if (isHome(subcommand)) {
            return service.suggestHome(sender, args);
        }
        if (isDelHome(subcommand)) {
            return service.suggestDelHome(sender, args);
        }
        if (isHomes(subcommand)) {
            return service.suggestHomes(sender, args);
        }
        if (isRenameHome(subcommand)) {
            return service.suggestRenameHome(sender, args);
        }
        return List.of();
    }

    public static List<String> subcommandNames() {
        List<String> names = new ArrayList<>();
        names.add("sethome");
        names.add(HomeSystem.ALIAS_SETHOME);
        names.add("home");
        names.add(HomeSystem.ALIAS_HOME_1);
        names.add(HomeSystem.ALIAS_HOME_2);
        names.add("delhome");
        names.add("remhome");
        names.add(HomeSystem.ALIAS_DELHOME);
        names.add("homes");
        names.add("renamehome");
        names.add(HomeSystem.ALIAS_RENAME_1);
        names.add(HomeSystem.ALIAS_RENAME_2);
        return names;
    }

    public static boolean isHomeSubcommand(String arg) {
        return isSetHome(arg) || isHome(arg) || isDelHome(arg) || isHomes(arg) || isRenameHome(arg);
    }

    public static boolean isSetHome(String arg) {
        return arg.equalsIgnoreCase("sethome") || arg.equalsIgnoreCase(HomeSystem.ALIAS_SETHOME);
    }

    public static boolean isHome(String arg) {
        return arg.equalsIgnoreCase("home")
                || arg.equalsIgnoreCase(HomeSystem.ALIAS_HOME_1)
                || arg.equalsIgnoreCase(HomeSystem.ALIAS_HOME_2);
    }

    public static boolean isDelHome(String arg) {
        return arg.equalsIgnoreCase("delhome")
                || arg.equalsIgnoreCase("remhome")
                || arg.equalsIgnoreCase(HomeSystem.ALIAS_DELHOME);
    }

    public static boolean isHomes(String arg) {
        return arg.equalsIgnoreCase("homes");
    }

    public static boolean isRenameHome(String arg) {
        return arg.equalsIgnoreCase("renamehome")
                || arg.equalsIgnoreCase(HomeSystem.ALIAS_RENAME_1)
                || arg.equalsIgnoreCase(HomeSystem.ALIAS_RENAME_2);
    }

    public static List<String> filterSubcommands(String prefix) {
        return CommandSuggestions.filter(subcommandNames(), prefix);
    }
}
