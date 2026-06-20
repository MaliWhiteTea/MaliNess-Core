package com.mertaliakcay.malinesscore.systems.playtime;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;

public final class PlaytimeBasicCommand implements BasicCommand {

    private final PlaytimeCommand playtimeCommand;

    public PlaytimeBasicCommand(PlaytimeCommand playtimeCommand) {
        this.playtimeCommand = playtimeCommand;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        playtimeCommand.handle(source.getSender(), args);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return true;
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        return playtimeCommand.suggest(source.getSender(), args);
    }
}
