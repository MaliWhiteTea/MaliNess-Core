package com.mertaliakcay.malinesscore.systems.playtime;

import com.mertaliakcay.malinesscore.MaliNessCore;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class PlaytimeStorage {

    private final MaliNessCore plugin;
    private final File folder;
    private final Map<UUID, Long> cache = new ConcurrentHashMap<>();

    public PlaytimeStorage(MaliNessCore plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "data/playtime");
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    public long getTotalMillis(UUID playerId) {
        Long cached = cache.get(playerId);
        if (cached != null) {
            return cached;
        }

        File file = getFile(playerId);
        if (!file.exists()) {
            return 0L;
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        long total = yaml.getLong("total-millis", 0L);
        cache.put(playerId, total);
        return total;
    }

    public void setTotalMillis(UUID playerId, long totalMillis) {
        cache.put(playerId, Math.max(0L, totalMillis));
    }

    public void save(UUID playerId) {
        Long total = cache.get(playerId);
        if (total == null) {
            return;
        }

        File file = getFile(playerId);
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("total-millis", total);

        try {
            yaml.save(file);
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "Oynama süresi kaydedilemedi: " + playerId, exception);
        }
    }

    public void flushAll() {
        for (UUID playerId : cache.keySet()) {
            save(playerId);
        }
    }

    private File getFile(UUID playerId) {
        return new File(folder, playerId + ".yml");
    }
}
