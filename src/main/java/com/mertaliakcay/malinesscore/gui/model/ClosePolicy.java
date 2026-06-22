package com.mertaliakcay.malinesscore.gui.model;

public enum ClosePolicy {
    NORMAL,
    MANDATORY;

    public static ClosePolicy fromString(String value) {
        if (value == null) {
            return NORMAL;
        }
        return switch (value.toLowerCase()) {
            case "mandatory" -> MANDATORY;
            default -> NORMAL;
        };
    }
}
