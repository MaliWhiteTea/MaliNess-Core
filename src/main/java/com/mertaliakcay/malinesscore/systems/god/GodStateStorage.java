package com.mertaliakcay.malinesscore.systems.god;

import com.mertaliakcay.malinesscore.MaliNessCore;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

final class GodStateStorage {

    private final MaliNessCore plugin;
    private final File cacheFile;

    GodStateStorage(MaliNessCore plugin) {
        this.plugin = plugin;
        this.cacheFile = new File(plugin.getDataFolder(), "data/god-reload-cache.yml");
        File parent = cacheFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
    }

    boolean save(Collection<UUID> godPlayers) {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("players", godPlayers.stream().map(UUID::toString).toList());

        File tempFile = new File(cacheFile.getParentFile(), cacheFile.getName() + ".tmp");
        try {
            yaml.save(tempFile);
            try {
                Files.move(tempFile.toPath(), cacheFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(tempFile.toPath(), cacheFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "God modu reload onbellegi kaydedilemedi.", exception);
            if (tempFile.exists() && !tempFile.delete()) {
                plugin.getLogger().warning("God cache gecici dosyasi silinemedi: " + tempFile.getPath());
            }
            return false;
        }
    }

    Set<UUID> load() {
        if (!cacheFile.exists()) {
            return Set.of();
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(cacheFile);
        Set<UUID> restored = new HashSet<>();

        for (String uuidText : yaml.getStringList("players")) {
            try {
                restored.add(UUID.fromString(uuidText));
            } catch (IllegalArgumentException exception) {
                plugin.getLogger().warning("Gecersiz god cache UUID: " + uuidText);
            }
        }

        return restored;
    }

    void delete() {
        if (cacheFile.exists() && !cacheFile.delete()) {
            plugin.getLogger().warning("God modu reload onbellegi silinemedi: " + cacheFile.getPath());
        }
    }
}
