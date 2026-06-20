package com.mertaliakcay.malinesscore.systems.vanish;

import com.mertaliakcay.malinesscore.MaliNessCore;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public final class VanishStorage {

    private final MaliNessCore plugin;
    private final File file;
    private final Set<UUID> vanishedIds = new HashSet<>();

    public VanishStorage(MaliNessCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "data/vanish.yml");
    }

    public void load() {
        vanishedIds.clear();
        if (!file.exists()) {
            return;
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        for (String rawId : yaml.getStringList("vanished")) {
            try {
                vanishedIds.add(UUID.fromString(rawId));
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("Geçersiz vanish UUID atlandı: " + rawId);
            }
        }
    }

    public void save() {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("vanished", vanishedIds.stream().map(UUID::toString).sorted().toList());

        try {
            yaml.save(file);
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "Vanish durumu kaydedilemedi.", exception);
        }
    }

    public boolean isVanished(UUID playerId) {
        return vanishedIds.contains(playerId);
    }

    public void setVanished(UUID playerId, boolean vanished) {
        if (vanished) {
            vanishedIds.add(playerId);
        } else {
            vanishedIds.remove(playerId);
        }
    }

    public Set<UUID> getVanishedIds() {
        return Collections.unmodifiableSet(vanishedIds);
    }
}
