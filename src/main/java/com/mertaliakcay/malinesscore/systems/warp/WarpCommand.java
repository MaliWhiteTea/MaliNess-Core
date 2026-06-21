package com.mertaliakcay.malinesscore.systems.warp;

import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public final class WarpCommand {

    private final WarpSystem system;

    public WarpCommand(WarpSystem system) {
        this.system = system;
    }

    public void handle(CommandSender sender, String[] args) {
        WarpService service = system.getWarpService();
        if (service == null) {
            system.getLang().send(sender, "system-disabled");
            return;
        }

        service.handle(sender, args);
    }

    public void handleList(CommandSender sender, String[] args) {
        WarpService service = system.getWarpService();
        if (service == null) {
            system.getLang().send(sender, "system-disabled");
            return;
        }

        service.handleList(sender, args);
    }

    public List<String> suggest(CommandSender sender, String[] args) {
        WarpService service = system.getWarpService();
        if (service == null || !system.isEnabled()) {
            return Collections.emptyList();
        }

        return service.suggest(sender, args);
    }

    public boolean canSuggest(CommandSender sender) {
        return system.isEnabled()
                && (sender.hasPermission(WarpSystem.PERM_USE)
                || sender.hasPermission(WarpSystem.PERM_MANAGE));
    }

    public boolean canSuggestList(CommandSender sender) {
        return system.isEnabled() && sender.hasPermission(WarpSystem.PERM_USE);
    }

    public List<String> suggestList(CommandSender sender, String[] args) {
        if (!canSuggestList(sender)) {
            return java.util.Collections.emptyList();
        }

        if (args.length <= 1) {
            return com.mertaliakcay.malinesscore.util.CommandSuggestions.filter(
                    List.of("1", "2", "3"),
                    args.length == 0 ? "" : args[0]
            );
        }

        return java.util.Collections.emptyList();
    }
}
