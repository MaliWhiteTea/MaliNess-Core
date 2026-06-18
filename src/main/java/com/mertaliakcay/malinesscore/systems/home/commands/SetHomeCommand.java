package com.mertaliakcay.malinesscore.systems.home.commands;

import com.mertaliakcay.malinesscore.systems.home.HomeService;
import com.mertaliakcay.malinesscore.systems.home.HomeSystem;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;

public final class SetHomeCommand implements BasicCommand {

    private final HomeService service;

    public SetHomeCommand(HomeService service) {
        this.service = service;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        service.handleSetHome(source.getSender(), args);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.hasPermission(HomeSystem.PERM_SETHOME);
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        return service.suggestSetHome(source.getSender(), args);
    }
}
