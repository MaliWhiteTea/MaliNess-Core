package com.mertaliakcay.malinesscore.systems.warp;

import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public final class WarpMnCommand {

    private final WarpCommand warpCommand;

    public WarpMnCommand(WarpCommand warpCommand) {
        this.warpCommand = warpCommand;
    }

    public static boolean isWarpSubcommand(String arg) {
        return arg.equalsIgnoreCase("warp");
    }

    public static boolean isWarpsListSubcommand(String arg) {
        return arg.equalsIgnoreCase("warps") || arg.equalsIgnoreCase(WarpSystem.ALIAS_WARPLAR);
    }

    public void handle(CommandSender sender, String[] args) {
        warpCommand.handle(sender, args);
    }

    public void handleList(CommandSender sender, String[] args) {
        warpCommand.handleList(sender, args);
    }

    public List<String> suggest(CommandSender sender, String[] args) {
        if (!warpCommand.canSuggest(sender)) {
            return Collections.emptyList();
        }
        return warpCommand.suggest(sender, args);
    }

    public List<String> suggestList(CommandSender sender, String[] args) {
        if (!warpCommand.canSuggestList(sender)) {
            return Collections.emptyList();
        }
        return warpCommand.suggestList(sender, args);
    }
}
