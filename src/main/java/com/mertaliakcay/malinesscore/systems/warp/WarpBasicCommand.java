package com.mertaliakcay.malinesscore.systems.warp;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;

public final class WarpBasicCommand implements BasicCommand {

    private final WarpCommand warpCommand;

    public WarpBasicCommand(WarpCommand warpCommand) {
        this.warpCommand = warpCommand;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        warpCommand.handle(source.getSender(), args);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return true;
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        if (!warpCommand.canSuggest(source.getSender())) {
            return List.of();
        }
        return warpCommand.suggest(source.getSender(), args);
    }
}
