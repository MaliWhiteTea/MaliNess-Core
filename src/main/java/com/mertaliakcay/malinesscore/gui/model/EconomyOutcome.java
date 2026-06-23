package com.mertaliakcay.malinesscore.gui.model;

public enum EconomyOutcome {
    CLOSE,
    STAY;

    public static EconomyOutcome fromString(String value, EconomyOutcome fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return EconomyOutcome.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            return fallback;
        }
    }
}
