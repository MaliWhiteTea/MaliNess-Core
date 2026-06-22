package com.mertaliakcay.malinesscore.gui.model;

public enum VisibilityRule {
    ALWAYS,
    MIN_PAGE,
    HAS_NEXT_PAGE,
    HAS_PREV_PAGE;

    public static VisibilityRule parse(String value) {
        if (value == null || value.isBlank() || value.equalsIgnoreCase("always")) {
            return ALWAYS;
        }
        String lower = value.toLowerCase();
        if (lower.startsWith("min-page:")) {
            return MIN_PAGE;
        }
        return switch (lower) {
            case "has-next-page" -> HAS_NEXT_PAGE;
            case "has-prev-page", "min-page:2" -> HAS_PREV_PAGE;
            default -> ALWAYS;
        };
    }

    public static int parseMinPage(String value) {
        if (value == null || !value.toLowerCase().startsWith("min-page:")) {
            return 2;
        }
        try {
            return Integer.parseInt(value.substring("min-page:".length()).trim());
        } catch (NumberFormatException exception) {
            return 2;
        }
    }
}
