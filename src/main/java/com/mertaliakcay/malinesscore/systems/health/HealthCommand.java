package com.mertaliakcay.malinesscore.systems.health;

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

public final class HealthCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = List.of("set", "add", "remove", "ayarla", "ekle", "azalt");

    private enum Action {
        SET, ADD, REMOVE
    }

    private final HealthSystem system;

    public HealthCommand(HealthSystem system) {
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

        if (!sender.hasPermission(HealthSystem.PERM_USE)) {
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
        if (!system.isEnabled() || !sender.hasPermission(HealthSystem.PERM_USE)) {
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

    public boolean canSuggest(CommandSender sender) {
        return system.isEnabled() && sender.hasPermission(HealthSystem.PERM_USE);
    }

    private void handleSet(CommandSender sender, Player target, int amount) {
        if (amount < HealthSystem.MIN_SET || amount > HealthSystem.MAX_SET) {
            system.getLang().send(sender, "invalid-set-amount", "amount", amount);
            return;
        }

        target.setHealth(amount);
        system.getLang().send(sender, "health-set", "player", target.getName(), "amount", amount);
    }

    private void handleAdd(CommandSender sender, Player target, int amount) {
        if (amount < HealthSystem.MIN_ADD || amount > HealthSystem.MAX_ADD) {
            system.getLang().send(sender, "invalid-add-amount", "amount", amount);
            return;
        }

        int current = getCurrentHealth(target);
        int newHealth = Math.min(current + amount, HealthSystem.MAX_POINTS);
        target.setHealth(newHealth);
        system.getLang().send(sender, "health-add", "player", target.getName(), "amount", amount);
    }

    private void handleRemove(CommandSender sender, Player target, int amount) {
        if (amount < HealthSystem.MIN_REMOVE || amount > HealthSystem.MAX_REMOVE) {
            system.getLang().send(sender, "invalid-remove-amount", "amount", amount);
            return;
        }

        int current = getCurrentHealth(target);
        if (current <= 1) {
            system.getLang().send(sender, "remove-too-low", "player", target.getName(), "current", current);
            return;
        }

        if (amount > current) {
            system.getLang().send(sender, "remove-too-much", "player", target.getName(), "amount", amount, "current", current);
            return;
        }

        target.setHealth(current - amount);
        system.getLang().send(sender, "health-remove", "player", target.getName(), "amount", amount);
    }

    private Action parseAction(String arg) {
        return switch (arg.toLowerCase(Locale.ROOT)) {
            case "set", "ayarla" -> Action.SET;
            case "add", "ekle" -> Action.ADD;
            case "remove", "azalt" -> Action.REMOVE;
            default -> null;
        };
    }

    private int getCurrentHealth(Player player) {
        return (int) Math.floor(player.getHealth());
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
            case SET -> range(HealthSystem.MIN_SET, HealthSystem.MAX_SET);
            case ADD -> range(HealthSystem.MIN_ADD, HealthSystem.MAX_ADD);
            case REMOVE -> range(HealthSystem.MIN_REMOVE, HealthSystem.MAX_REMOVE);
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
