package com.mertaliakcay.malinesscore.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class CommandSuggestions {

    private CommandSuggestions() {
    }

    public static List<String> filter(Collection<String> options, String input) {
        if (input == null || input.isEmpty()) {
            return new ArrayList<>(options);
        }

        String lower = input.toLowerCase(Locale.ROOT);
        return options.stream()
                .filter(option -> option.toLowerCase(Locale.ROOT).startsWith(lower))
                .collect(Collectors.toList());
    }

    public static boolean isExactMatch(String input, Collection<String> options) {
        if (input == null || input.isEmpty()) {
            return false;
        }

        return options.stream().anyMatch(option -> option.equalsIgnoreCase(input));
    }

    public static List<String> onlinePlayerNames(CommandSender sender, String prefix, boolean excludeSender) {
        String lower = prefix == null ? "" : prefix.toLowerCase(Locale.ROOT);
        List<String> names = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (excludeSender && sender instanceof Player self && player.getUniqueId().equals(self.getUniqueId())) {
                continue;
            }
            if (player.getName().toLowerCase(Locale.ROOT).startsWith(lower)) {
                names.add(player.getName());
            }
        }
        return names;
    }
}
