package com.mertaliakcay.malinesscore.systems.saturation;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;

public final class SaturationBasicCommand implements BasicCommand {

    private final SaturationCommand saturationCommand;

    public SaturationBasicCommand(SaturationCommand saturationCommand) {
        this.saturationCommand = saturationCommand;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        saturationCommand.handle(source.getSender(), args);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.hasPermission(SaturationSystem.PERM_USE);
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        return saturationCommand.suggest(source.getSender(), args);
    }
}
