package com.mertaliakcay.malinesscore.systems.hunger;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;

public final class HungerBasicCommand implements BasicCommand {

    private final HungerCommand hungerCommand;

    public HungerBasicCommand(HungerCommand hungerCommand) {
        this.hungerCommand = hungerCommand;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        hungerCommand.handle(source.getSender(), args);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return true;
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        if (!hungerCommand.canSuggest(source.getSender())) {
            return List.of();
        }
        return hungerCommand.suggest(source.getSender(), args);
    }
}
