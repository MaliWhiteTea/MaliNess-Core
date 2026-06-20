package com.mertaliakcay.malinesscore.systems.broadcast;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;

public final class BroadcastBasicCommand implements BasicCommand {

    private final BroadcastCommand broadcastCommand;

    public BroadcastBasicCommand(BroadcastCommand broadcastCommand) {
        this.broadcastCommand = broadcastCommand;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        broadcastCommand.handle(source.getSender(), args);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return true;
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        return broadcastCommand.suggest(source.getSender(), args);
    }
}
