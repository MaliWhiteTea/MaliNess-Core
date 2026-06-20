package com.mertaliakcay.malinesscore.systems.playtime;

import org.bukkit.command.CommandSender;

import java.util.List;

public final class PlaytimeMnCommand {

    public static final String ALIAS_TURKISH = "oynamasüresi";

    private final PlaytimeCommand playtimeCommand;

    public PlaytimeMnCommand(PlaytimeCommand playtimeCommand) {
        this.playtimeCommand = playtimeCommand;
    }

    public void handle(CommandSender sender, String[] args) {
        playtimeCommand.handle(sender, args);
    }

    public List<String> suggest(CommandSender sender, String[] args) {
        return playtimeCommand.suggest(sender, args);
    }

    public static boolean isPlaytimeSubcommand(String arg) {
        return arg.equalsIgnoreCase("playtime") || arg.equalsIgnoreCase(ALIAS_TURKISH);
    }
}
