package com.mertaliakcay.malinesscore.systems.playtime;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class PlaytimeFormatConfig {

    private final String separator;
    private final String emptyFallback;
    private final boolean showZero;
    private final List<PlaytimeUnit> units;

    PlaytimeFormatConfig(ConfigurationSection section) {
        this.separator = section.getString("separator", " ");
        this.emptyFallback = section.getString("empty-fallback", "0sn");
        this.showZero = section.getBoolean("show-zero", false);

        ConfigurationSection unitsSection = section.getConfigurationSection("units");
        Map<String, PlaytimeUnit> unitMap = new LinkedHashMap<>();
        if (unitsSection != null) {
            for (String key : unitsSection.getKeys(false)) {
                ConfigurationSection unitSection = unitsSection.getConfigurationSection(key);
                if (unitSection == null) {
                    continue;
                }

                String code = unitSection.getString("code", key);
                long seconds = unitSection.getLong("seconds", 1L);
                unitMap.put(key.toLowerCase(), new PlaytimeUnit(code, seconds));
            }
        }

        List<String> order = section.getStringList("order");
        this.units = new ArrayList<>();
        for (String key : order) {
            PlaytimeUnit unit = unitMap.get(key.toLowerCase());
            if (unit != null) {
                this.units.add(unit);
            }
        }

        if (this.units.isEmpty()) {
            unitMap.values().stream()
                    .sorted(Comparator.comparingLong(PlaytimeUnit::seconds).reversed())
                    .forEach(this.units::add);
        }
    }

    String format(long totalSeconds) {
        if (totalSeconds <= 0L) {
            return emptyFallback;
        }

        long remaining = totalSeconds;
        List<String> parts = new ArrayList<>();

        for (PlaytimeUnit unit : units) {
            if (unit.seconds() <= 0L) {
                continue;
            }

            long value = remaining / unit.seconds();
            remaining %= unit.seconds();

            if (value > 0L) {
                parts.add(value + unit.code());
            } else if (showZero) {
                parts.add("0" + unit.code());
            }
        }

        if (parts.isEmpty()) {
            return emptyFallback;
        }

        return String.join(separator, parts);
    }

    record PlaytimeUnit(String code, long seconds) {
    }
}
