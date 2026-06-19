package com.mertaliakcay.malinesscore.systems.home;

import org.bukkit.configuration.ConfigurationSection;

final class HomeDataValidator {

    private static final double MAX_COORD = 30_000_000D;

    private HomeDataValidator() {
    }

    static ValidationResult validate(String rawName, ConfigurationSection section, HomeNameValidator nameValidator) {
        if (section == null) {
            return ValidationResult.invalid("ev bolumu bos");
        }

        String normalizedName = nameValidator.normalize(rawName);
        if (!nameValidator.isValid(normalizedName)) {
            return ValidationResult.invalid("gecersiz ev adi: " + rawName);
        }

        String worldName = section.getString("world");
        if (worldName == null || worldName.isBlank()) {
            return ValidationResult.invalid("dunya adi bos: " + rawName);
        }

        if (!section.isSet("x") || !section.isSet("y") || !section.isSet("z")) {
            return ValidationResult.invalid("eksik koordinat: " + rawName);
        }

        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");
        float yaw = section.isSet("yaw") ? (float) section.getDouble("yaw") : 0F;
        float pitch = section.isSet("pitch") ? (float) section.getDouble("pitch") : 0F;

        if (!isFiniteCoordinate(x) || !isFiniteCoordinate(y) || !isFiniteCoordinate(z)) {
            return ValidationResult.invalid("gecersiz koordinat: " + rawName);
        }

        if (!Float.isFinite(yaw) || !Float.isFinite(pitch)) {
            return ValidationResult.invalid("gecersiz bakis acisi: " + rawName);
        }

        return ValidationResult.valid(normalizedName, worldName.trim(), x, y, z, yaw, pitch, section.getLong("created", System.currentTimeMillis()));
    }

    private static boolean isFiniteCoordinate(double value) {
        return Double.isFinite(value) && value >= -MAX_COORD && value <= MAX_COORD;
    }

    record ValidationResult(
            boolean valid,
            String normalizedName,
            String worldName,
            double x,
            double y,
            double z,
            float yaw,
            float pitch,
            long createdAt,
            String reason
    ) {
        static ValidationResult valid(
                String normalizedName,
                String worldName,
                double x,
                double y,
                double z,
                float yaw,
                float pitch,
                long createdAt
        ) {
            return new ValidationResult(true, normalizedName, worldName, x, y, z, yaw, pitch, createdAt, null);
        }

        static ValidationResult invalid(String reason) {
            return new ValidationResult(false, null, null, 0, 0, 0, 0, 0, 0, reason);
        }
    }
}
