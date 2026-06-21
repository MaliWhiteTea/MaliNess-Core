package com.mertaliakcay.malinesscore.systems.pwarp;

import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public final class PwarpMnCommand {

    private final PwarpCommand pwarpCommand;

    public PwarpMnCommand(PwarpCommand pwarpCommand) {
        this.pwarpCommand = pwarpCommand;
    }

    public static boolean isPwarpSubcommand(String arg) {
        return arg.equalsIgnoreCase("pwarp");
    }

    public static boolean isPwarpsListSubcommand(String arg) {
        return arg.equalsIgnoreCase("pwarps");
    }

    public void handle(CommandSender sender, String[] args) {
        pwarpCommand.handle(sender, args);
    }

    public void handleList(CommandSender sender, String[] args) {
        pwarpCommand.handleList(sender, args);
    }

    public List<String> suggest(CommandSender sender, String[] args) {
        if (!pwarpCommand.canSuggest(sender)) {
            return Collections.emptyList();
        }
        return pwarpCommand.suggest(sender, args);
    }

    public List<String> suggestList(CommandSender sender, String[] args) {
        if (!pwarpCommand.canSuggestList(sender)) {
            return Collections.emptyList();
        }
        return pwarpCommand.suggestList(sender, args);
    }
}
