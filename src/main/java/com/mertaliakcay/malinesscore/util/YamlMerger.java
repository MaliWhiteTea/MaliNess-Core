package com.mertaliakcay.malinesscore.util;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

/**
 * Jar içindeki varsayılan YAML dosyası ile sunucudaki dosyayı birleştirir.
 * Sadece eksik anahtarlar eklenir; mevcut değerler asla değiştirilmez.
 */
public final class YamlMerger {

    private YamlMerger() {
    }

    public static YamlConfiguration loadAndMerge(JavaPlugin plugin, File file, String resourcePath) {
        ensureFileExists(plugin, file, resourcePath);

        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        YamlConfiguration defaults = loadResource(plugin, resourcePath);

        if (defaults == null) {
            return configuration;
        }

        int added = mergeMissing(configuration, defaults);
        if (added > 0) {
            try {
                configuration.save(file);
                plugin.getLogger().info(file.getName() + " dosyasına " + added + " yeni ayar eklendi.");
            } catch (IOException exception) {
                plugin.getLogger().log(Level.SEVERE, "Dosya kaydedilemedi: " + file.getName(), exception);
            }
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

        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        YamlConfiguration defaults = loadResource(plugin, resourcePath);

        if (defaults == null) {
            return;
        }

        int added = mergeMissing(configuration, defaults);
        if (added > 0) {
            try {
                configuration.save(file);
                plugin.getLogger().info(file.getName() + " dosyasına " + added + " yeni ayar eklendi.");
            } catch (IOException exception) {
                plugin.getLogger().log(Level.SEVERE, "Dosya kaydedilemedi: " + file.getName(), exception);
            }
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

    private static YamlConfiguration loadResource(JavaPlugin plugin, String resourcePath) {
        InputStream stream = plugin.getResource(resourcePath);
        if (stream == null) {
            return null;
        }

        return YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }
}
