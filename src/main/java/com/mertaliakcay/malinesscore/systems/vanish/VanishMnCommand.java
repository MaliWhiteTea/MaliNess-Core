package com.mertaliakcay.malinesscore.systems.vanish;

import org.bukkit.command.CommandSender;

import java.util.List;

public final class VanishMnCommand {

    public static final String ALIAS_TURKISH = "gizlen";

    private final VanishCommand vanishCommand;

    public VanishMnCommand(VanishCommand vanishCommand) {
        this.vanishCommand = vanishCommand;
    }

    public void handle(CommandSender sender, String[] args) {
        vanishCommand.handle(sender, args);
    }

    public List<String> suggest(CommandSender sender, String[] args) {
        return vanishCommand.suggest(sender, args);
    }

    public static boolean isVanishSubcommand(String arg) {
        return arg.equalsIgnoreCase("vanish") || arg.equalsIgnoreCase(ALIAS_TURKISH);
    }
}
