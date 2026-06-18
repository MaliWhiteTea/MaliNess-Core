package com.mertaliakcay.malinesscore.systems.saturate;

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
import java.util.stream.Collectors;

public final class SaturateCommand implements CommandExecutor, TabCompleter {

    private static final int MAX_FOOD_LEVEL = 20;
    private static final float MAX_SATURATION = 20.0F;

    private final SaturateSystem system;

    public SaturateCommand(SaturateSystem system) {
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

        if (args.length == 0) {
            handleSelfFullSaturate(sender);
            return;
        }

        if (args.length == 1) {
            if (isPositiveInteger(args[0])) {
                handleSelfPartialSaturate(sender, Integer.parseInt(args[0]));
            } else {
                handleOtherFullSaturate(sender, args[0]);
            }
            return;
        }

        if (args.length == 2) {
            if (!isPositiveInteger(args[0])) {
                system.getLang().send(sender, "invalid-amount", "amount", args[0]);
                return;
            }
            handleOtherPartialSaturate(sender, Integer.parseInt(args[0]), args[1]);
            return;
        }

        system.getLang().send(sender, "usage");
    }

    public List<String> suggest(CommandSender sender, String[] args) {
        if (!system.isEnabled()) {
            return Collections.emptyList();
        }

        if (args.length == 0) {
            return CommandSuggestions.filter(firstArgSuggestions(sender), "");
        }

        if (args.length == 1) {
            if (isPositiveInteger(args[0]) && sender.hasPermission(SaturateSystem.PERM_OTHERS)) {
                return CommandSuggestions.filter(onlinePlayerNames(), "");
            }
            return CommandSuggestions.filter(firstArgSuggestions(sender), args[0]);
        }

        if (args.length == 2 && sender.hasPermission(SaturateSystem.PERM_OTHERS)) {
            return CommandSuggestions.filter(onlinePlayerNames(), args[1]);
        }

        return Collections.emptyList();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return suggest(sender, args);
    }

    private void handleSelfFullSaturate(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            system.getLang().send(sender, "console-no-saturation");
            return;
        }

        if (!sender.hasPermission(SaturateSystem.PERM_USE)) {
            system.getLang().send(sender, "no-permission");
            return;
        }

        saturateFully(player);
        system.getLang().send(sender, "saturated-self-full");
    }

    private void handleSelfPartialSaturate(CommandSender sender, int points) {
        if (!(sender instanceof Player player)) {
            system.getLang().send(sender, "console-no-saturation");
            return;
        }

        if (!sender.hasPermission(SaturateSystem.PERM_USE)) {
            system.getLang().send(sender, "no-permission");
            return;
        }

        if (points <= 0) {
            system.getLang().send(sender, "invalid-amount", "amount", points);
            return;
        }

        saturatePartial(player, points);
        system.getLang().send(sender, "saturated-self-partial", "amount", points);
    }

    private void handleOtherFullSaturate(CommandSender sender, String targetName) {
        if (!sender.hasPermission(SaturateSystem.PERM_OTHERS)) {
            system.getLang().send(sender, "no-permission-others");
            return;
        }

        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            system.getLang().send(sender, "player-not-found", "player", targetName);
            return;
        }

        saturateFully(target);
        system.getLang().send(sender, "saturated-other-full", "player", target.getName());
    }

    private void handleOtherPartialSaturate(CommandSender sender, int points, String targetName) {
        if (!sender.hasPermission(SaturateSystem.PERM_OTHERS)) {
            system.getLang().send(sender, "no-permission-others");
            return;
        }

        if (points <= 0) {
            system.getLang().send(sender, "invalid-amount", "amount", points);
            return;
        }

        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            system.getLang().send(sender, "player-not-found", "player", targetName);
            return;
        }

        saturatePartial(target, points);
        system.getLang().send(sender, "saturated-other-partial", "player", target.getName(), "amount", points);
    }

    private void saturateFully(Player player) {
        player.setSaturation(MAX_SATURATION);
        fillHunger(player);
    }

    private void saturatePartial(Player player, int points) {
        float newSaturation = Math.min(player.getSaturation() + points, MAX_SATURATION);
        player.setSaturation(newSaturation);
        fillHunger(player);
    }

    private void fillHunger(Player player) {
        player.setFoodLevel(MAX_FOOD_LEVEL);
    }

    private List<String> firstArgSuggestions(CommandSender sender) {
        List<String> suggestions = new ArrayList<>();

        if (sender.hasPermission(SaturateSystem.PERM_USE)) {
            suggestions.add("2");
            suggestions.add("4");
            suggestions.add("10");
            suggestions.add("20");
        }

        if (sender.hasPermission(SaturateSystem.PERM_OTHERS)) {
            suggestions.addAll(onlinePlayerNames());
        }

        return suggestions;
    }

    private List<String> onlinePlayerNames() {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList());
    }

    private boolean isPositiveInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }
}
