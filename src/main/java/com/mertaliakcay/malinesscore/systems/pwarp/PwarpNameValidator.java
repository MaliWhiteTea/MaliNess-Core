package com.mertaliakcay.malinesscore.systems.pwarp;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class PwarpNameValidator {

    private static final Locale TURKISH = Locale.forLanguageTag("tr-TR");
    private static final Pattern VALID_NAME = Pattern.compile("^[\\p{L}\\p{N}_-]{2,20}$");
    private static final Pattern HAS_LETTER = Pattern.compile(".*\\p{L}.*");

    private final List<String> reservedNames;
    private final List<String> blacklist;

    public PwarpNameValidator(List<String> reservedNames, List<String> blacklist) {
        this.reservedNames = reservedNames.stream()
                .map(name -> name.toLowerCase(TURKISH))
                .toList();
        this.blacklist = blacklist.stream()
                .map(name -> name.toLowerCase(TURKISH))
                .toList();
    }

    public static String canonicalKey(String rawName) {
        if (rawName == null) {
            return "";
        }
        return rawName.toLowerCase(TURKISH);
    }

    public boolean isValid(String rawName) {
        if (rawName == null || rawName.isBlank()) {
            return false;
        }

        String trimmed = rawName.trim();
        String key = canonicalKey(trimmed);
        return VALID_NAME.matcher(trimmed).matches()
                && HAS_LETTER.matcher(trimmed).matches()
                && !reservedNames.contains(key)
                && !blacklist.contains(key);
    }

    public boolean isReservedKeyword(String rawName) {
        String key = canonicalKey(rawName);
        return reservedNames.contains(key) || blacklist.contains(key);
    }
}
