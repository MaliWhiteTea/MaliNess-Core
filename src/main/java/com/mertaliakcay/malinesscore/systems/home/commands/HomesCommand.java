package com.mertaliakcay.malinesscore.systems.home.commands;

import com.mertaliakcay.malinesscore.systems.home.HomeService;
import com.mertaliakcay.malinesscore.systems.home.HomeSystem;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;

public final class HomesCommand implements BasicCommand {

    private final HomeService service;

    public HomesCommand(HomeService service) {
        this.service = service;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        service.handleHomes(source.getSender(), args);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.hasPermission(HomeSystem.PERM_HOMES)
                || sender.hasPermission(HomeSystem.PERM_OTHERS_LIST);
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        return service.suggestHomes(source.getSender(), args);
    }
}
