package com.mertaliakcay.malinesscore.systems.feed;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;

public final class FeedBasicCommand implements BasicCommand {

    private final FeedCommand feedCommand;

    public FeedBasicCommand(FeedCommand feedCommand) {
        this.feedCommand = feedCommand;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        feedCommand.handle(source.getSender(), args);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return true;
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        if (!feedCommand.canSuggest(source.getSender())) {
            return List.of();
        }
        return feedCommand.suggest(source.getSender(), args);
    }
}
