package com.mertaliakcay.malinesscore.command;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;

public final class MnguiBasicCommand implements BasicCommand {

    private final MnguiCommand command;

    public MnguiBasicCommand(MnguiCommand command) {
        this.command = command;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        command.handle(source.getSender(), args);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return true;
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        return command.suggest(source.getSender(), args);
    }
}
