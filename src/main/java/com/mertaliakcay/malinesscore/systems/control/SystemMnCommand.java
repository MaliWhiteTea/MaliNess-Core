package com.mertaliakcay.malinesscore.systems.control;

import com.mertaliakcay.malinesscore.MaliNessCore;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public final class SystemMnCommand {

    private final MaliNessCore plugin;
    private final SystemControlService controlService;

    public SystemMnCommand(MaliNessCore plugin, SystemControlService controlService) {
        this.plugin = plugin;
        this.controlService = controlService;
    }

    public void handleList(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getPluginLang().send(sender, "systems-console-unsupported");
            return;
        }

        if (!controlService.canList(sender)) {
            plugin.getPluginLang().send(sender, "systems-no-list-permission");
            return;
        }

        int page = 1;
        if (args.length >= 1) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
                plugin.getPluginLang().send(sender, "systems-list-invalid-page");
                return;
            }
        }

        SystemsListHelp.send(plugin, controlService, sender, page);
    }

    public void handleSystem(CommandSender sender, String[] args) {
        controlService.handleSystemCommand(sender, args);
    }

    public List<String> suggestSystem(String[] args) {
        if (args.length == 0 || args.length == 1) {
            String prefix = args.length == 1 ? args[0] : "";
            return filterPrefix(SystemControlService.actionSuggestions(), prefix);
        }
        return List.of();
    }

    public List<String> suggestSystemNested(CommandSender sender, String[] args) {
        if (args.length == 0 || args.length == 1) {
            String prefix = args.length == 1 ? args[0] : "";
            return filterPrefix(SystemControlService.actionSuggestions(), prefix);
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

    public static boolean isSystemsSubcommand(String arg) {
        return arg.equalsIgnoreCase("systems") || arg.equalsIgnoreCase(SystemControlService.ALIAS_SYSTEMS_TR);
    }

    public static boolean isSystemSubcommand(String arg) {
        return arg.equalsIgnoreCase("system") || arg.equalsIgnoreCase(SystemControlService.ALIAS_SYSTEM_TR);
    }

    private List<String> filterPrefix(List<String> values, String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return values;
        }
        String lower = prefix.toLowerCase();
        return values.stream()
                .filter(value -> value.toLowerCase().startsWith(lower))
                .toList();
    }
}
