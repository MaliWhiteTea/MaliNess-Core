package com.mertaliakcay.malinesscore.util;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

/**
 * Jar içindeki varsayılan YAML dosyası ile sunucudaki dosyayı birleştirir.
 * Sadece eksik anahtarlar eklenir; mevcut değerler asla değiştirilmez.
 * Lang dosyaları için {@link #loadAndMergeLang} kullanılır (lang-version ile senkron).
 */
public final class YamlMerger {

    private static final String LANG_VERSION_KEY = "lang-version";

    private YamlMerger() {
    }

    public static YamlConfiguration loadAndMerge(JavaPlugin plugin, File file, String resourcePath) {
        ensureFileExists(plugin, file, resourcePath);

        YamlConfiguration configuration = loadFile(file);
        YamlConfiguration defaults = loadResource(plugin, resourcePath);

        if (defaults == null) {
            return configuration;
        }

        int added = mergeMissing(configuration, defaults);
        if (added > 0) {
            saveConfiguration(plugin, file, configuration, added + " yeni ayar eklendi.");
        }

        return configuration;
    }

    public static YamlConfiguration loadAndMergeLang(JavaPlugin plugin, File file, String resourcePath) {
        ensureFileExists(plugin, file, resourcePath);

        YamlConfiguration configuration = loadFile(file);
        YamlConfiguration defaults = loadResource(plugin, resourcePath);

        if (defaults == null) {
            return configuration;
        }

        int defaultVersion = defaults.getInt(LANG_VERSION_KEY, 1);
        int currentVersion = configuration.getInt(LANG_VERSION_KEY, 0);
        boolean changed = false;

        if (defaultVersion > currentVersion) {
            int synced = syncAllValues(configuration, defaults);
            configuration.set(LANG_VERSION_KEY, defaultVersion);
            plugin.getLogger().info(
                    file.getName() + " güncellendi (lang sürümü " + currentVersion + " → " + defaultVersion
                            + ", " + synced + " mesaj senkronize edildi)."
            );
            changed = true;
        }

        int added = mergeMissing(configuration, defaults);
        if (added > 0) {
            saveConfiguration(plugin, file, configuration, added + " yeni mesaj eklendi.");
            return configuration;
        }

        if (changed) {
            saveConfiguration(plugin, file, configuration, null);
        }

        return configuration;
    }

    public static void mergeMainConfig(JavaPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "config.yml");
        plugin.saveDefaultConfig();
        mergeIntoExisting(plugin, file, "config.yml");
        plugin.reloadConfig();
    }

    public static void mergeIntoExisting(JavaPlugin plugin, File file, String resourcePath) {
        if (!file.exists()) {
            return;
        }

        YamlConfiguration configuration = loadFile(file);
        YamlConfiguration defaults = loadResource(plugin, resourcePath);

        if (defaults == null) {
            return;
        }

        int added = mergeMissing(configuration, defaults);
        if (added > 0) {
            saveConfiguration(plugin, file, configuration, added + " yeni ayar eklendi.");
        }
    }

    public static int mergeMissing(YamlConfiguration target, YamlConfiguration defaults) {
        int added = 0;

        for (String path : defaults.getKeys(true)) {
            if (defaults.isConfigurationSection(path)) {
                continue;
            }

            if (!target.contains(path)) {
                target.set(path, defaults.get(path));
                added++;
            }
        }

        return added;
    }

    public static int syncAllValues(YamlConfiguration target, YamlConfiguration defaults) {
        int synced = 0;

        for (String path : defaults.getKeys(true)) {
            if (defaults.isConfigurationSection(path)) {
                continue;
            }

            target.set(path, defaults.get(path));
            synced++;
        }

        return synced;
    }

    private static YamlConfiguration loadFile(File file) {
        if (!file.exists()) {
            return new YamlConfiguration();
        }

        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            return YamlConfiguration.loadConfiguration(reader);
        } catch (IOException exception) {
            YamlConfiguration fallback = YamlConfiguration.loadConfiguration(file);
            if (fallback != null) {
                return fallback;
            }
            return new YamlConfiguration();
        }
    }

    private static void ensureFileExists(JavaPlugin plugin, File file, String resourcePath) {
        if (file.exists()) {
            return;
        }

        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        if (plugin.getResource(resourcePath) != null) {
            plugin.saveResource(resourcePath, false);
            return;
        }

        try {
            if (!file.createNewFile()) {
                plugin.getLogger().warning("Dosya oluşturulamadı: " + file.getName());
            }
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "Dosya oluşturulamadı: " + file.getName(), exception);
        }
    }

    private static void saveConfiguration(JavaPlugin plugin, File file, YamlConfiguration configuration, String detail) {
        try {
            configuration.save(file);
            if (detail != null) {
                plugin.getLogger().info(file.getName() + " dosyasına " + detail);
            }
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "Dosya kaydedilemedi: " + file.getName(), exception);
        }
    }

    private static YamlConfiguration loadResource(JavaPlugin plugin, String resourcePath) {
        InputStream stream = plugin.getResource(resourcePath);
        if (stream == null) {
            return null;
        }

        return YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }
}
