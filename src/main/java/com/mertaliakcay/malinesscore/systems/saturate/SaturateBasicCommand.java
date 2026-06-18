package com.mertaliakcay.malinesscore.systems.saturate;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;

public final class SaturateBasicCommand implements BasicCommand {

    private final SaturateCommand saturateCommand;

    public SaturateBasicCommand(SaturateCommand saturateCommand) {
        this.saturateCommand = saturateCommand;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        saturateCommand.handle(source.getSender(), args);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.hasPermission(SaturateSystem.PERM_USE) || sender.hasPermission(SaturateSystem.PERM_OTHERS);
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        return saturateCommand.suggest(source.getSender(), args);
    }
}
