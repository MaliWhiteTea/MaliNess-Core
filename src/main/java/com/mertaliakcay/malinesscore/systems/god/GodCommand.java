package com.mertaliakcay.malinesscore.systems.god;

import com.mertaliakcay.malinesscore.util.CommandSuggestions;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class GodCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SET_SUBCOMMANDS = List.of("ayarla", "set");
    private static final List<String> STATE_OPTIONS = List.of(
            "aktif", "deaktif", "active", "deactivate", "on", "off"
    );

    private final GodSystem system;

    public GodCommand(GodSystem system) {
        this.system = system;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        handle(sender, args);
        return true;
    }

    public void handle(CommandSender sender, String[] args) {
        if (!system.isEnabled()) {
            system.getLang().send(sender, "system-disabled");
            return;
        }

        if (sender instanceof Player player && !hasAnyGodPermission(sender)) {
            system.getLang().send(sender, "no-permission");
            return;
        }

        if (args.length == 0) {
            handleSelfToggle(sender);
            return;
        }

        if (isSetSubcommand(args[0])) {
            handleSet(sender, args);
            return;
        }

        if (args.length == 1) {
            handleOtherToggle(sender, args[0]);
            return;
        }

        system.getLang().send(sender, "usage");
    }

    public List<String> suggest(CommandSender sender, String[] args) {
        if (!system.isEnabled() || !canSuggest(sender)) {
            return Collections.emptyList();
        }

        if (args.length == 0) {
            return CommandSuggestions.filter(firstArgSuggestions(sender), "");
        }

        if (args.length == 1) {
            if (CommandSuggestions.isExactMatch(args[0], SET_SUBCOMMANDS)) {
                return CommandSuggestions.filter(onlinePlayerNames(sender), "");
            }
            return CommandSuggestions.filter(firstArgSuggestions(sender), args[0]);
        }

        if (args.length == 2 && isSetSubcommand(args[0])) {
            if (CommandSuggestions.isExactMatch(args[1], onlinePlayerNames(sender))) {
                return CommandSuggestions.filter(STATE_OPTIONS, "");
            }
            return CommandSuggestions.filter(onlinePlayerNames(sender), args[1]);
        }

        if (args.length == 3 && isSetSubcommand(args[0])) {
            return CommandSuggestions.filter(STATE_OPTIONS, args[2]);
        }

        return Collections.emptyList();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return suggest(sender, args);
    }

    private void handleSelfToggle(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            system.getLang().send(sender, "console-no-player");
            return;
        }

        if (!sender.hasPermission(GodSystem.PERM_USE)) {
            if (sender.hasPermission(GodSystem.PERM_OTHERS)) {
                system.getLang().send(sender, "usage-others-required");
            } else {
                system.getLang().send(sender, "no-permission");
            }
            return;
        }

        boolean enabled = system.toggleGod(player);
        sendToggleMessage(sender, player, enabled);
    }

    private void handleOtherToggle(CommandSender sender, String targetName) {
        if (!sender.hasPermission(GodSystem.PERM_OTHERS)) {
            system.getLang().send(sender, "no-permission-others");
            return;
        }

        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            system.getLang().send(sender, "player-not-found", "player", targetName);
            return;
        }

        boolean enabled = system.toggleGod(target);
        sendToggleMessage(sender, target, enabled);
    }

    private void handleSet(CommandSender sender, String[] args) {
        if (!sender.hasPermission(GodSystem.PERM_OTHERS)) {
            system.getLang().send(sender, "no-permission-others");
            return;
        }

        if (args.length != 3) {
            system.getLang().send(sender, "usage-set");
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            system.getLang().send(sender, "player-not-found", "player", args[1]);
            return;
        }

        Boolean state = parseState(args[2]);
        if (state == null) {
            system.getLang().send(sender, "invalid-state", "state", args[2]);
            return;
        }

        system.setGod(target, state);
        sendSetMessage(sender, target, state);
    }

    private void sendToggleMessage(CommandSender sender, Player target, boolean enabled) {
        if (sender.equals(target)) {
            if (enabled) {
                system.getLang().send(sender, "god-enabled-self");
            } else {
                system.getLang().send(sender, "god-disabled-self");
            }
            return;
        }

        if (enabled) {
            system.getLang().send(sender, "god-enabled-other", "player", target.getName());
            system.getLang().send(target, "god-enabled-target");
        } else {
            system.getLang().send(sender, "god-disabled-other", "player", target.getName());
            system.getLang().send(target, "god-disabled-target");
        }
    }

    private void sendSetMessage(CommandSender sender, Player target, boolean enabled) {
        if (enabled) {
            system.getLang().send(sender, "god-enabled-other", "player", target.getName());
            if (!sender.equals(target)) {
                system.getLang().send(target, "god-enabled-target");
            }
        } else {
            system.getLang().send(sender, "god-disabled-other", "player", target.getName());
            if (!sender.equals(target)) {
                system.getLang().send(target, "god-disabled-target");
            }
        }
    }

    private List<String> firstArgSuggestions(CommandSender sender) {
        List<String> suggestions = new ArrayList<>(SET_SUBCOMMANDS);

        if (sender.hasPermission(GodSystem.PERM_OTHERS)) {
            suggestions.addAll(onlinePlayerNames(sender));
        }

        return suggestions;
    }

    private List<String> onlinePlayerNames(CommandSender sender) {
        List<String> names = Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList());

        if (system.getPlugin().getVanishService() != null) {
            return system.getPlugin().getVanishService().filterPlayerNames(sender, names);
        }

        return names;
    }

    private boolean isSetSubcommand(String arg) {
        return SET_SUBCOMMANDS.stream().anyMatch(option -> option.equalsIgnoreCase(arg));
    }

    private boolean hasAnyGodPermission(CommandSender sender) {
        return sender.hasPermission(GodSystem.PERM_USE) || sender.hasPermission(GodSystem.PERM_OTHERS);
    }

    public boolean canSuggest(CommandSender sender) {
        return system.isEnabled() && (!(sender instanceof Player) || hasAnyGodPermission(sender));
    }

    private Boolean parseState(String arg) {
        return switch (arg.toLowerCase(Locale.ROOT)) {
            case "aktif", "active", "on", "enable", "true" -> true;
            case "deaktif", "deactivate", "off", "disable", "false" -> false;
            default -> null;
        };
    }
}
