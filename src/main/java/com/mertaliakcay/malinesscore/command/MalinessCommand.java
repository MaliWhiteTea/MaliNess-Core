package com.mertaliakcay.malinesscore.command;

import com.mertaliakcay.malinesscore.systems.heal.HealCommand;
import com.mertaliakcay.malinesscore.systems.heal.HealSystem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class MalinessCommand implements CommandExecutor, TabCompleter {

    private final HealSystem healSystem;
    private HealCommand healCommand;

    public MalinessCommand(HealSystem healSystem) {
        this.healSystem = healSystem;
    }

    public void setHealCommand(HealCommand healCommand) {
        this.healCommand = healCommand;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            healSystem.getLang().send(sender, "root-usage");
            return true;
        }

        if (args[0].equalsIgnoreCase("heal")) {
            if (healCommand == null) {
                healSystem.getLang().send(sender, "system-disabled");
                return true;
            }

            String[] healArgs = Arrays.copyOfRange(args, 1, args.length);
            return healCommand.onCommand(sender, command, label, healArgs);
        }

        healSystem.getLang().send(sender, "unknown-subcommand", "subcommand", args[0]);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subcommands = new ArrayList<>();
            if (healSystem.isEnabled()) {
                subcommands.add("heal");
            }
            return filter(subcommands, args[0]);
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("heal") && healCommand != null) {
            String[] healArgs = Arrays.copyOfRange(args, 1, args.length);
            List<String> suggestions = healCommand.onTabComplete(sender, command, alias, healArgs);
            return suggestions == null ? Collections.emptyList() : suggestions;
        }

        return Collections.emptyList();
    }

    private List<String> filter(List<String> options, String input) {
        String lower = input.toLowerCase(Locale.ROOT);
        return options.stream()
                .filter(option -> option.toLowerCase(Locale.ROOT).startsWith(lower))
                .collect(Collectors.toList());
    }
}
