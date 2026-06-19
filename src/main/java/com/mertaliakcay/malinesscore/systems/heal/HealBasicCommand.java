package com.mertaliakcay.malinesscore.systems.heal;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;

public final class HealBasicCommand implements BasicCommand {

    private final HealCommand healCommand;

    public HealBasicCommand(HealCommand healCommand) {
        this.healCommand = healCommand;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        healCommand.handle(source.getSender(), args);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return true;
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        if (!healCommand.canSuggest(source.getSender())) {
            return List.of();
        }
        return healCommand.suggest(source.getSender(), args);
    }
}
