package com.mertaliakcay.malinesscore.systems.economy.command;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public final class EconomyBasicCommand implements BasicCommand {

    private final BiConsumer<CommandSender, String[]> executor;
    private final BiFunction<CommandSender, String[], List<String>> suggest;

    public EconomyBasicCommand(
            BiConsumer<CommandSender, String[]> executor,
            BiFunction<CommandSender, String[], List<String>> suggest
    ) {
        this.executor = executor;
        this.suggest = suggest;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        executor.accept(source.getSender(), args);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return true;
    }

    @Override
    public List<String> suggest(CommandSourceStack source, String[] args) {
        return suggest.apply(source.getSender(), args);
    }
}
