package com.mertaliakcay.malinesscore.systems.hunger;

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

public final class HungerCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = List.of("set", "add", "remove", "ayarla", "ekle", "azalt");

    private enum Action {
        SET, ADD, REMOVE
    }

    private final HungerSystem system;

    public HungerCommand(HungerSystem system) {
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

        if (!sender.hasPermission(HungerSystem.PERM_USE)) {
            system.getLang().send(sender, "no-permission");
            return;
        }

        if (args.length != 3) {
            system.getLang().send(sender, "usage");
            return;
        }

        Action action = parseAction(args[0]);
        if (action == null) {
            system.getLang().send(sender, "invalid-subcommand", "subcommand", args[0]);
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            system.getLang().send(sender, "player-not-found", "player", args[1]);
            return;
        }

        Integer amount = parsePositiveInteger(args[2]);
        if (amount == null) {
            system.getLang().send(sender, "invalid-amount", "amount", args[2]);
            return;
        }

        switch (action) {
            case SET -> handleSet(sender, target, amount);
            case ADD -> handleAdd(sender, target, amount);
            case REMOVE -> handleRemove(sender, target, amount);
        }
    }

    public List<String> suggest(CommandSender sender, String[] args) {
        if (!system.isEnabled() || !sender.hasPermission(HungerSystem.PERM_USE)) {
            return Collections.emptyList();
        }

        if (args.length == 0) {
            return CommandSuggestions.filter(SUBCOMMANDS, "");
        }

        if (args.length == 1) {
            if (CommandSuggestions.isExactMatch(args[0], SUBCOMMANDS)) {
                return CommandSuggestions.filter(onlinePlayerNames(), "");
            }
            return CommandSuggestions.filter(SUBCOMMANDS, args[0]);
        }

        if (args.length == 2) {
            Action action = parseAction(args[0]);
            if (action == null) {
                return Collections.emptyList();
            }
            if (CommandSuggestions.isExactMatch(args[1], onlinePlayerNames())) {
                return CommandSuggestions.filter(amountRange(action), "");
            }
            return CommandSuggestions.filter(onlinePlayerNames(), args[1]);
        }

        if (args.length == 3) {
            Action action = parseAction(args[0]);
            if (action == null) {
                return Collections.emptyList();
            }

            return CommandSuggestions.filter(amountRange(action), args[2]);
        }

        return Collections.emptyList();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return suggest(sender, args);
    }

    private void handleSet(CommandSender sender, Player target, int amount) {
        if (amount < HungerSystem.MIN_SET || amount > HungerSystem.MAX_SET) {
            system.getLang().send(sender, "invalid-set-amount", "amount", amount);
            return;
        }

        target.setFoodLevel(amount);
        system.getLang().send(sender, "hunger-set", "player", target.getName(), "amount", amount);
    }

    private void handleAdd(CommandSender sender, Player target, int amount) {
        if (amount < HungerSystem.MIN_ADD) {
            system.getLang().send(sender, "invalid-add-amount", "amount", amount);
            return;
        }

        int current = target.getFoodLevel();
        if (amount > HungerSystem.MAX_POINTS || current + amount > HungerSystem.MAX_POINTS) {
            target.setFoodLevel(HungerSystem.MAX_POINTS);
            system.getLang().send(sender, "hunger-set", "player", target.getName(), "amount", HungerSystem.MAX_POINTS);
            return;
        }

        target.setFoodLevel(current + amount);
        system.getLang().send(sender, "hunger-add", "player", target.getName(), "amount", amount);
    }

    private void handleRemove(CommandSender sender, Player target, int amount) {
        if (amount < HungerSystem.MIN_REMOVE || amount > HungerSystem.MAX_REMOVE) {
            system.getLang().send(sender, "invalid-remove-amount", "amount", amount);
            return;
        }

        int current = target.getFoodLevel();
        if (amount > current) {
            system.getLang().send(sender, "remove-too-much", "player", target.getName(), "amount", amount, "current", current);
            return;
        }

        target.setFoodLevel(current - amount);
        system.getLang().send(sender, "hunger-remove", "player", target.getName(), "amount", amount);
    }

    private Action parseAction(String arg) {
        return switch (arg.toLowerCase(Locale.ROOT)) {
            case "set", "ayarla" -> Action.SET;
            case "add", "ekle" -> Action.ADD;
            case "remove", "azalt" -> Action.REMOVE;
            default -> null;
        };
    }

    private Integer parsePositiveInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private List<String> amountRange(Action action) {
        return switch (action) {
            case SET -> range(HungerSystem.MIN_SET, HungerSystem.MAX_SET);
            case ADD -> range(HungerSystem.MIN_ADD, HungerSystem.MAX_ADD);
            case REMOVE -> range(HungerSystem.MIN_REMOVE, HungerSystem.MAX_REMOVE);
        };
    }

    private List<String> onlinePlayerNames() {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList());
    }

    private List<String> range(int min, int max) {
        List<String> values = new ArrayList<>();
        for (int i = min; i <= max; i++) {
            values.add(String.valueOf(i));
        }
        return values;
    }
}
