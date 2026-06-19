package com.mertaliakcay.malinesscore.systems.control;

import com.mertaliakcay.malinesscore.MaliNessCore;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class SystemBasicCommand implements BasicCommand {

    private final MaliNessCore plugin;
    private final SystemControlService controlService;

    public SystemBasicCommand(MaliNessCore plugin, SystemControlService controlService) {
        this.plugin = plugin;
        this.controlService = controlService;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        controlService.handleSystemCommand(source.getSender(), args);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return true;
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        CommandSender sender = source.getSender();

        if (args.length == 0 || args.length == 1) {
            String prefix = args.length == 1 ? args[0] : "";
            return filter(SystemControlService.actionSuggestions(), prefix);
        }

        if (args.length == 2) {
            String action = SystemControlService.normalizeAction(args[0]);
            if (action == null) {
                return List.of();
            }
            if ("info".equals(action)) {
                return controlService.systemIdSuggestions(sender, args[1]);
            }
            return controlService.manageableSystemSuggestions(sender, args[1]);
        }

        return List.of();
    }

    private List<String> filter(List<String> values, String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return values;
        }
        String lower = prefix.toLowerCase();
        List<String> filtered = new ArrayList<>();
        for (String value : values) {
            if (value.toLowerCase().startsWith(lower)) {
                filtered.add(value);
            }
        }
        return filtered;
    }
}
