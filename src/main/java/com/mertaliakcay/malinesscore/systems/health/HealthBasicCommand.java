package com.mertaliakcay.malinesscore.systems.health;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;

public final class HealthBasicCommand implements BasicCommand {

    private final HealthCommand healthCommand;

    public HealthBasicCommand(HealthCommand healthCommand) {
        this.healthCommand = healthCommand;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        healthCommand.handle(source.getSender(), args);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.hasPermission(HealthSystem.PERM_USE);
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        return healthCommand.suggest(source.getSender(), args);
    }
}
