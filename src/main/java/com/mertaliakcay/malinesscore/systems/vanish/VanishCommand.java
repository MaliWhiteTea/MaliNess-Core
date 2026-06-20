package com.mertaliakcay.malinesscore.systems.vanish;

import com.mertaliakcay.malinesscore.util.CommandSuggestions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class VanishCommand {

    private static final String LIST_SUBCOMMAND = "list";

    private final VanishSystem system;

    public VanishCommand(VanishSystem system) {
        this.system = system;
    }

    public void handle(CommandSender sender, String[] args) {
        if (!system.isEnabled()) {
            system.getLang().send(sender, "system-disabled");
            return;
        }

        if (args.length == 0) {
            handleSelfToggle(sender);
            return;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase(LIST_SUBCOMMAND)) {
            handleList(sender);
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
            return CommandSuggestions.filter(baseSuggestions(sender), "");
        }

        if (args.length == 1) {
            return CommandSuggestions.filter(baseSuggestions(sender), args[0]);
        }

        return Collections.emptyList();
    }

    public boolean canSuggest(CommandSender sender) {
        return system.isEnabled()
                && (sender.hasPermission(VanishSystem.PERM_USE)
                || sender.hasPermission(VanishSystem.PERM_OTHERS)
                || sender.hasPermission(VanishSystem.PERM_SEE));
    }

    private void handleSelfToggle(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            system.getLang().send(sender, "console-no-player");
            return;
        }

        if (!sender.hasPermission(VanishSystem.PERM_USE)) {
            system.getLang().send(sender, "no-permission");
            return;
        }

        boolean enabled = !system.getVanishService().isVanished(player);
        setVanish(sender, player, enabled);
    }

    private void handleOtherToggle(CommandSender sender, String targetName) {
        if (!sender.hasPermission(VanishSystem.PERM_OTHERS)) {
            system.getLang().send(sender, "no-permission-others");
            return;
        }

        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            system.getLang().send(sender, "player-not-found", "player", targetName);
            return;
        }

        boolean enabled = !system.getVanishService().isVanished(target);
        setVanish(sender, target, enabled);
    }

    private void handleList(CommandSender sender) {
        if (!sender.hasPermission(VanishSystem.PERM_SEE)) {
            system.getLang().send(sender, "no-permission-list");
            return;
        }

        List<Player> vanishedPlayers = system.getVanishService().getOnlineVanishedPlayers();
        if (vanishedPlayers.isEmpty()) {
            system.getLang().send(sender, "list-empty");
            return;
        }

        system.getLang().send(sender, "list-header", "count", vanishedPlayers.size());
        for (Player player : vanishedPlayers) {
            system.getLang().send(sender, "list-entry", "player", player.getName());
        }
    }

    private void setVanish(CommandSender sender, Player target, boolean enabled) {
        if (enabled) {
            system.getVanishService().enableVanish(target);
        } else {
            system.getVanishService().disableVanish(target);
        }

        sendToggleMessage(sender, target, enabled);
    }

    private void sendToggleMessage(CommandSender sender, Player target, boolean enabled) {
        if (sender.equals(target)) {
            system.getLang().send(sender, enabled ? "enabled-self" : "disabled-self");
            return;
        }

        system.getLang().send(sender, enabled ? "enabled-other" : "disabled-other", "player", target.getName());
        system.getLang().send(target, enabled ? "enabled-target" : "disabled-target");
    }

    private List<String> baseSuggestions(CommandSender sender) {
        List<String> suggestions = new ArrayList<>();
        if (sender.hasPermission(VanishSystem.PERM_SEE)) {
            suggestions.add(LIST_SUBCOMMAND);
        }
        if (sender.hasPermission(VanishSystem.PERM_OTHERS)) {
            suggestions.addAll(onlinePlayerNames(sender));
        }
        return suggestions;
    }

    private List<String> onlinePlayerNames(CommandSender sender) {
        return Bukkit.getOnlinePlayers().stream()
                .filter(player -> system.getVanishService().canSee(sender, player))
                .map(Player::getName)
                .collect(Collectors.toList());
    }
}
