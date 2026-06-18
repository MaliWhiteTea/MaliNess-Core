package com.mertaliakcay.malinesscore.systems.heal;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
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

public final class HealCommand implements CommandExecutor, TabCompleter {

    private final HealSystem system;

    public HealCommand(HealSystem system) {
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
            handleSelfFullHeal(sender);
            return;
        }

        if (args.length == 1) {
            if (isPositiveInteger(args[0])) {
                handleSelfPartialHeal(sender, Integer.parseInt(args[0]));
            } else {
                handleOtherFullHeal(sender, args[0]);
            }
            return;
        }

        if (args.length == 2) {
            if (!isPositiveInteger(args[0])) {
                system.getLang().send(sender, "invalid-amount", "amount", args[0]);
                return;
            }
            handleOtherPartialHeal(sender, Integer.parseInt(args[0]), args[1]);
            return;
        }

        system.getLang().send(sender, "usage");
    }

    public List<String> suggest(CommandSender sender, String[] args) {
        if (!system.isEnabled()) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();

            if (sender.hasPermission(HealSystem.PERM_USE)) {
                suggestions.add("2");
                suggestions.add("4");
                suggestions.add("10");
                suggestions.add("20");
            }

            if (sender.hasPermission(HealSystem.PERM_OTHERS)) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    suggestions.add(player.getName());
                }
            }

            return filter(suggestions, args[0]);
        }

        if (args.length == 2 && sender.hasPermission(HealSystem.PERM_OTHERS)) {
            List<String> playerNames = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
            return filter(playerNames, args[1]);
        }

        return Collections.emptyList();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return suggest(sender, args);
    }

    private boolean handleSelfFullHeal(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            system.getLang().send(sender, "console-no-health");
            return true;
        }

        if (!sender.hasPermission(HealSystem.PERM_USE)) {
            system.getLang().send(sender, "no-permission");
            return true;
        }

        healFully(player);
        system.getLang().send(sender, "healed-self-full");
        return true;
    }

    private boolean handleSelfPartialHeal(CommandSender sender, int halfHearts) {
        if (!(sender instanceof Player player)) {
            system.getLang().send(sender, "console-no-health");
            return true;
        }

        if (!sender.hasPermission(HealSystem.PERM_USE)) {
            system.getLang().send(sender, "no-permission");
            return true;
        }

        if (halfHearts <= 0) {
            system.getLang().send(sender, "invalid-amount", "amount", halfHearts);
            return true;
        }

        healPartial(player, halfHearts);
        system.getLang().send(sender, "healed-self-partial", "amount", halfHearts);
        return true;
    }

    private boolean handleOtherFullHeal(CommandSender sender, String targetName) {
        if (!sender.hasPermission(HealSystem.PERM_OTHERS)) {
            system.getLang().send(sender, "no-permission-others");
            return true;
        }

        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            system.getLang().send(sender, "player-not-found", "player", targetName);
            return true;
        }

        healFully(target);
        system.getLang().send(sender, "healed-other-full", "player", target.getName());
        return true;
    }

    private boolean handleOtherPartialHeal(CommandSender sender, int halfHearts, String targetName) {
        if (!sender.hasPermission(HealSystem.PERM_OTHERS)) {
            system.getLang().send(sender, "no-permission-others");
            return true;
        }

        if (halfHearts <= 0) {
            system.getLang().send(sender, "invalid-amount", "amount", halfHearts);
            return true;
        }

        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            system.getLang().send(sender, "player-not-found", "player", targetName);
            return true;
        }

        healPartial(target, halfHearts);
        system.getLang().send(sender, "healed-other-partial", "player", target.getName(), "amount", halfHearts);
        return true;
    }

    private void healFully(Player player) {
        double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
        player.setHealth(maxHealth);
    }

    private void healPartial(Player player, int halfHearts) {
        double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
        double newHealth = Math.min(player.getHealth() + halfHearts, maxHealth);
        player.setHealth(newHealth);
    }

    private boolean isPositiveInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    private List<String> filter(List<String> options, String input) {
        String lower = input.toLowerCase(Locale.ROOT);
        return options.stream()
                .filter(option -> option.toLowerCase(Locale.ROOT).startsWith(lower))
                .collect(Collectors.toList());
    }
}
