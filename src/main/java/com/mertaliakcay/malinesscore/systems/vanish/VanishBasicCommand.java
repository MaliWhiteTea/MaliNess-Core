package com.mertaliakcay.malinesscore.systems.vanish;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;

public final class VanishBasicCommand implements BasicCommand {

    private final VanishCommand vanishCommand;

    public VanishBasicCommand(VanishCommand vanishCommand) {
        this.vanishCommand = vanishCommand;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        vanishCommand.handle(source.getSender(), args);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return true;
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        return vanishCommand.suggest(source.getSender(), args);
    }
}
