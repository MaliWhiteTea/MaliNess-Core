package com.mertaliakcay.malinesscore.systems.pwarp;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;

public final class PwarpsBasicCommand implements BasicCommand {

    private final PwarpCommand pwarpCommand;

    public PwarpsBasicCommand(PwarpCommand pwarpCommand) {
        this.pwarpCommand = pwarpCommand;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        pwarpCommand.handleList(source.getSender(), args);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return true;
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        if (!pwarpCommand.canSuggestList(source.getSender())) {
            return List.of();
        }
        return pwarpCommand.suggestList(source.getSender(), args);
    }
}
