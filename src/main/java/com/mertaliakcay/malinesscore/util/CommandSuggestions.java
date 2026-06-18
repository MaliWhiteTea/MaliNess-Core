package com.mertaliakcay.malinesscore.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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
}
