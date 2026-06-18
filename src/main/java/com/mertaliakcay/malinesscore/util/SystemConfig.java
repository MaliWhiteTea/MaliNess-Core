package com.mertaliakcay.malinesscore.util;

import com.mertaliakcay.malinesscore.MaliNessCore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Her sistem kendi config dosyasını kullanır: configs/&lt;sistem&gt;.yml
 */
public final class SystemConfig {

    private final MaliNessCore plugin;
    private final String systemId;
    private File configFile;
    private FileConfiguration config;

    public SystemConfig(MaliNessCore plugin, String systemId) {
        this.plugin = plugin;
        this.systemId = systemId;
        reload();
    }

    public String getSystemId() {
        return systemId;
    }

    public FileConfiguration get() {
        return config;
    }

    public void reload() {
        File folder = new File(plugin.getDataFolder(), "configs");
        if (!folder.exists() && !folder.mkdirs()) {
            plugin.getLogger().warning("configs klasörü oluşturulamadı.");
        }

        configFile = new File(folder, systemId + ".yml");
        String resourcePath = "configs/" + systemId + ".yml";

        if (plugin.getResource(resourcePath) != null) {
            config = YamlMerger.loadAndMerge(plugin, configFile, resourcePath);
        } else {
            if (!configFile.exists()) {
                try {
                    if (!configFile.createNewFile()) {
                        plugin.getLogger().warning("Config dosyası oluşturulamadı: " + configFile.getName());
                    }
                } catch (IOException exception) {
                    plugin.getLogger().log(Level.SEVERE, "Config dosyası oluşturulamadı: " + configFile.getName(), exception);
                }
            }
            config = YamlConfiguration.loadConfiguration(configFile);
        }
    }

    public void save() {
        try {
            config.save(configFile);
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "Config kaydedilemedi: " + configFile.getName(), exception);
        }
    }
}
