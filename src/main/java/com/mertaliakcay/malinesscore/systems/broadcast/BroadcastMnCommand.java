package com.mertaliakcay.malinesscore.systems.broadcast;

import org.bukkit.command.CommandSender;

import java.util.List;

public final class BroadcastMnCommand {

    private final BroadcastCommand broadcastCommand;

    public BroadcastMnCommand(BroadcastCommand broadcastCommand) {
        this.broadcastCommand = broadcastCommand;
    }

    public void handle(CommandSender sender, String[] args) {
        broadcastCommand.handle(sender, args);
    }

    public List<String> suggest(CommandSender sender, String[] args) {
        return broadcastCommand.suggest(sender, args);
    }

    public static boolean isBroadcastSubcommand(String arg) {
        return arg.equalsIgnoreCase("broadcast")
                || arg.equalsIgnoreCase(BroadcastSystem.ALIAS_BC)
                || arg.equalsIgnoreCase(BroadcastSystem.ALIAS_DUYUR)
                || arg.equalsIgnoreCase(BroadcastSystem.ALIAS_DUYURUYAP);
    }
}
