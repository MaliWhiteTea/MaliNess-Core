package com.mertaliakcay.malinesscore.gui.model;

import org.bukkit.event.inventory.ClickType;

import java.util.Locale;
import java.util.Optional;

public enum MenuClickType {
    LEFT,
    RIGHT,
    SHIFT_LEFT,
    SHIFT_RIGHT,
    MIDDLE,
    DROP,
    CONTROL_DROP,
    SWAP_OFFHAND,
    NUMBER_KEY_1,
    NUMBER_KEY_2,
    NUMBER_KEY_3,
    NUMBER_KEY_4,
    NUMBER_KEY_5,
    NUMBER_KEY_6,
    NUMBER_KEY_7,
    NUMBER_KEY_8,
    NUMBER_KEY_9;

    public static Optional<MenuClickType> fromBukkit(ClickType clickType, int hotbarButton) {
        if (clickType == null) {
            return Optional.empty();
        }
        return switch (clickType) {
            case LEFT -> Optional.of(LEFT);
            case RIGHT -> Optional.of(RIGHT);
            case SHIFT_LEFT -> Optional.of(SHIFT_LEFT);
            case SHIFT_RIGHT -> Optional.of(SHIFT_RIGHT);
            case MIDDLE -> Optional.of(MIDDLE);
            case DROP -> Optional.of(DROP);
            case CONTROL_DROP -> Optional.of(CONTROL_DROP);
            case SWAP_OFFHAND -> Optional.of(SWAP_OFFHAND);
            case NUMBER_KEY -> Optional.of(numberKey(hotbarButton));
            default -> Optional.empty();
        };
    }

    public static MenuClickType fromYamlKey(String key) {
        String normalized = key.toLowerCase(Locale.ROOT).replace('_', '-');
        if (normalized.startsWith("number-key-")) {
            int hotbarKey = Integer.parseInt(normalized.substring("number-key-".length()).trim());
            if (hotbarKey >= 1 && hotbarKey <= 9) {
                return numberKey(hotbarKey - 1);
            }
            throw new IllegalArgumentException("number-key must be 1-9: " + hotbarKey);
        }
        return switch (normalized) {
            case "right" -> RIGHT;
            case "shift-left" -> SHIFT_LEFT;
            case "shift-right" -> SHIFT_RIGHT;
            case "middle" -> MIDDLE;
            case "drop" -> DROP;
            case "control-drop" -> CONTROL_DROP;
            case "swap-offhand" -> SWAP_OFFHAND;
            default -> LEFT;
        };
    }

    public String toYamlKey() {
        return name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    private static MenuClickType numberKey(int hotbarButton) {
        int slot = Math.clamp(hotbarButton, 0, 8);
        return values()[8 + slot];
    }
}
