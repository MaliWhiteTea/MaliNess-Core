package com.mertaliakcay.malinesscore.gui.model;

public enum DynamicCountType {
    NONE,
    PREV_REMAINING,
    NEXT_REMAINING;

    public static DynamicCountType fromString(String value) {
        if (value == null) {
            return NONE;
        }
        return switch (value.toLowerCase()) {
            case "prev-remaining", "prev-pages" -> PREV_REMAINING;
            case "next-remaining", "next-pages" -> NEXT_REMAINING;
            default -> NONE;
        };
    }
}
