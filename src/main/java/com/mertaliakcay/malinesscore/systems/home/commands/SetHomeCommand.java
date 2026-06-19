package com.mertaliakcay.malinesscore.systems.home.commands;

import com.mertaliakcay.malinesscore.systems.home.HomeService;
import com.mertaliakcay.malinesscore.systems.home.HomeSystem;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;

public final class SetHomeCommand implements BasicCommand {

    private final HomeSystem system;

    public SetHomeCommand(HomeSystem system) {
        this.system = system;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        HomeService service = HomeCommandSupport.requireService(system, source.getSender());
        if (service != null) {
            service.handleSetHome(source.getSender(), args);
        }
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return true;
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        if (!system.isActive() || !HomeCommandSupport.canSuggestSetHome(source.getSender())) {
            return List.of();
        }
        HomeService service = system.getHomeService();
        return service == null ? List.of() : service.suggestSetHome(source.getSender(), args);
    }
}
