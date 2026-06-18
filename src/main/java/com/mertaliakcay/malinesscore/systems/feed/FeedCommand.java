package com.mertaliakcay.malinesscore.systems.feed;

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

public final class FeedCommand implements CommandExecutor, TabCompleter {

    private static final int MAX_FOOD_LEVEL = 20;

    private final FeedSystem system;

    public FeedCommand(FeedSystem system) {
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
            handleSelfFullFeed(sender);
            return;
        }

        if (args.length == 1) {
            if (isPositiveInteger(args[0])) {
                handleSelfPartialFeed(sender, Integer.parseInt(args[0]));
            } else {
                handleOtherFullFeed(sender, args[0]);
            }
            return;
        }

        if (args.length == 2) {
            if (!isPositiveInteger(args[0])) {
                system.getLang().send(sender, "invalid-amount", "amount", args[0]);
                return;
            }
            handleOtherPartialFeed(sender, Integer.parseInt(args[0]), args[1]);
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
            if (isPositiveInteger(args[0]) && sender.hasPermission(FeedSystem.PERM_OTHERS)) {
                return CommandSuggestions.filter(onlinePlayerNames(), "");
            }
            return CommandSuggestions.filter(firstArgSuggestions(sender), args[0]);
        }

        if (args.length == 2 && sender.hasPermission(FeedSystem.PERM_OTHERS)) {
            return CommandSuggestions.filter(onlinePlayerNames(), args[1]);
        }

        return Collections.emptyList();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return suggest(sender, args);
    }

    private void handleSelfFullFeed(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            system.getLang().send(sender, "console-no-hunger");
            return;
        }

        if (!sender.hasPermission(FeedSystem.PERM_USE)) {
            system.getLang().send(sender, "no-permission");
            return;
        }

        feedFully(player);
        system.getLang().send(sender, "fed-self-full");
    }

    private void handleSelfPartialFeed(CommandSender sender, int foodPoints) {
        if (!(sender instanceof Player player)) {
            system.getLang().send(sender, "console-no-hunger");
            return;
        }

        if (!sender.hasPermission(FeedSystem.PERM_USE)) {
            system.getLang().send(sender, "no-permission");
            return;
        }

        if (foodPoints <= 0) {
            system.getLang().send(sender, "invalid-amount", "amount", foodPoints);
            return;
        }

        feedPartial(player, foodPoints);
        system.getLang().send(sender, "fed-self-partial", "amount", foodPoints);
    }

    private void handleOtherFullFeed(CommandSender sender, String targetName) {
        if (!sender.hasPermission(FeedSystem.PERM_OTHERS)) {
            system.getLang().send(sender, "no-permission-others");
            return;
        }

        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            system.getLang().send(sender, "player-not-found", "player", targetName);
            return;
        }

        feedFully(target);
        system.getLang().send(sender, "fed-other-full", "player", target.getName());
    }

    private void handleOtherPartialFeed(CommandSender sender, int foodPoints, String targetName) {
        if (!sender.hasPermission(FeedSystem.PERM_OTHERS)) {
            system.getLang().send(sender, "no-permission-others");
            return;
        }

        if (foodPoints <= 0) {
            system.getLang().send(sender, "invalid-amount", "amount", foodPoints);
            return;
        }

        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            system.getLang().send(sender, "player-not-found", "player", targetName);
            return;
        }

        feedPartial(target, foodPoints);
        system.getLang().send(sender, "fed-other-partial", "player", target.getName(), "amount", foodPoints);
    }

    private void feedFully(Player player) {
        player.setFoodLevel(MAX_FOOD_LEVEL);
    }

    private void feedPartial(Player player, int foodPoints) {
        player.setFoodLevel(Math.min(player.getFoodLevel() + foodPoints, MAX_FOOD_LEVEL));
    }

    private List<String> firstArgSuggestions(CommandSender sender) {
        List<String> suggestions = new ArrayList<>();

        if (sender.hasPermission(FeedSystem.PERM_USE)) {
            suggestions.add("2");
            suggestions.add("4");
            suggestions.add("10");
            suggestions.add("20");
        }

        if (sender.hasPermission(FeedSystem.PERM_OTHERS)) {
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
