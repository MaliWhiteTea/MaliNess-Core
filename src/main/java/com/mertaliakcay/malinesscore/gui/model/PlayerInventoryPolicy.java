package com.mertaliakcay.malinesscore.gui.model;

public enum PlayerInventoryPolicy {
    LOCKED,
    ALLOWED;

    public static PlayerInventoryPolicy fromString(String value) {
        if (value == null) {
            return LOCKED;
        }
        return switch (value.toLowerCase()) {
            case "allowed" -> ALLOWED;
            default -> LOCKED;
        };
    }
}
