package com.mertaliakcay.malinesscore.systems.home;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class HomeNameValidator {

    private static final Pattern VALID_NAME = Pattern.compile("^[a-z0-9_-]{1,12}$");

    private final List<String> reservedNames;
    private final String defaultName;

    public HomeNameValidator(List<String> reservedNames, String defaultName) {
        this.reservedNames = reservedNames.stream().map(name -> name.toLowerCase(Locale.ROOT)).toList();
        this.defaultName = defaultName.toLowerCase(Locale.ROOT);
    }

    public String normalize(String rawName) {
        return rawName.toLowerCase(Locale.ROOT);
    }

    public boolean isValid(String normalizedName) {
        return VALID_NAME.matcher(normalizedName).matches() && !reservedNames.contains(normalizedName);
    }

    public String resolveDefaultName(PlayerHomesAccessor homes) {
        if (!homes.contains(defaultName)) {
            return defaultName;
        }

        int index = 2;
        while (homes.contains(defaultName + "-" + index)) {
            index++;
        }

        return defaultName + "-" + index;
    }

    public String getDefaultName() {
        return defaultName;
    }

    @FunctionalInterface
    public interface PlayerHomesAccessor {
        boolean contains(String name);
    }
}
