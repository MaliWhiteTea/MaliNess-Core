package com.mertaliakcay.malinesscore.systems.warp;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;

public final class WarpsBasicCommand implements BasicCommand {

    private final WarpCommand warpCommand;

    public WarpsBasicCommand(WarpCommand warpCommand) {
        this.warpCommand = warpCommand;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        warpCommand.handleList(source.getSender(), args);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return true;
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        if (!warpCommand.canSuggestList(source.getSender())) {
            return List.of();
        }
        return warpCommand.suggestList(source.getSender(), args);
    }
}
