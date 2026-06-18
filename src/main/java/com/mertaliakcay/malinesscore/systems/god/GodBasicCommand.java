package com.mertaliakcay.malinesscore.systems.god;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;

public final class GodBasicCommand implements BasicCommand {

    private final GodCommand godCommand;

    public GodBasicCommand(GodCommand godCommand) {
        this.godCommand = godCommand;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        godCommand.handle(source.getSender(), args);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.hasPermission(GodSystem.PERM_USE) || sender.hasPermission(GodSystem.PERM_OTHERS);
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        return godCommand.suggest(source.getSender(), args);
    }
}
