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

        Double x = readCoordinate(section, "x");
        Double y = readCoordinate(section, "y");
        Double z = readCoordinate(section, "z");
        if (x == null || y == null || z == null) {
            return ValidationResult.invalid("gecersiz koordinat tipi: " + rawName);
        }

        Float yaw = readAngle(section, "yaw", 0F);
        Float pitch = readAngle(section, "pitch", 0F);
        if (yaw == null || pitch == null) {
            return ValidationResult.invalid("gecersiz bakis acisi tipi: " + rawName);
        }

        if (!isFiniteCoordinate(x) || !isFiniteCoordinate(y) || !isFiniteCoordinate(z)) {
            return ValidationResult.invalid("gecersiz koordinat: " + rawName);
        }

        if (!Float.isFinite(yaw) || !Float.isFinite(pitch)) {
            return ValidationResult.invalid("gecersiz bakis acisi: " + rawName);
        }

        long createdAt = readCreatedAt(section);

        return ValidationResult.valid(normalizedName, worldName.trim(), x, y, z, yaw, pitch, createdAt);
    }

    private static Double readCoordinate(ConfigurationSection section, String key) {
        if (section.isDouble(key) || section.isInt(key)) {
            return section.getDouble(key);
        }
        return null;
    }

    private static Float readAngle(ConfigurationSection section, String key, float defaultValue) {
        if (!section.isSet(key)) {
            return defaultValue;
        }
        if (section.isDouble(key) || section.isInt(key)) {
            return (float) section.getDouble(key);
        }
        return null;
    }

    private static long readCreatedAt(ConfigurationSection section) {
        if (!section.isSet("created")) {
            return System.currentTimeMillis();
        }
        if (section.isLong("created") || section.isInt("created")) {
            return section.getLong("created");
        }
        return System.currentTimeMillis();
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
