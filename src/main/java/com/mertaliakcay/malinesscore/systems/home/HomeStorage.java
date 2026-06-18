package com.mertaliakcay.malinesscore.systems.home;

import com.mertaliakcay.malinesscore.MaliNessCore;
import com.mertaliakcay.malinesscore.systems.home.model.HomeLocation;
import com.mertaliakcay.malinesscore.systems.home.model.PlayerHomes;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

public final class HomeStorage {

    private final MaliNessCore plugin;
    private final File homesFolder;

    public HomeStorage(MaliNessCore plugin) {
        this.plugin = plugin;
        this.homesFolder = new File(plugin.getDataFolder(), "data/homes");
        if (!homesFolder.exists()) {
            homesFolder.mkdirs();
        }
    }

    public PlayerHomes load(UUID playerId) {
        File file = getFile(playerId);
        if (!file.exists()) {
            return new PlayerHomes();
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        PlayerHomes homes = new PlayerHomes();
        ConfigurationSection section = yaml.getConfigurationSection("homes");
        if (section == null) {
            return homes;
        }

        for (String name : section.getKeys(false)) {
            ConfigurationSection homeSection = section.getConfigurationSection(name);
            if (homeSection == null) {
                continue;
            }

            HomeLocation location = new HomeLocation(
                    homeSection.getString("world"),
                    homeSection.getDouble("x"),
                    homeSection.getDouble("y"),
                    homeSection.getDouble("z"),
                    (float) homeSection.getDouble("yaw"),
                    (float) homeSection.getDouble("pitch"),
                    homeSection.getLong("created", System.currentTimeMillis())
            );
            homes.put(name, location);
        }

        return homes;
    }

    public void saveAsync(UUID playerId, PlayerHomes homes) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> saveSync(playerId, homes));
    }

    public void saveSync(UUID playerId, PlayerHomes homes) {
        File file = getFile(playerId);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        YamlConfiguration yaml = new YamlConfiguration();
        for (var entry : homes.getHomes().entrySet()) {
            String path = "homes." + entry.getKey();
            HomeLocation location = entry.getValue();
            yaml.set(path + ".world", location.getWorldName());
            yaml.set(path + ".x", location.getX());
            yaml.set(path + ".y", location.getY());
            yaml.set(path + ".z", location.getZ());
            yaml.set(path + ".yaw", location.getYaw());
            yaml.set(path + ".pitch", location.getPitch());
            yaml.set(path + ".created", location.getCreatedAt());
        }

        try {
            yaml.save(file);
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "Ev verisi kaydedilemedi: " + playerId, exception);
        }
    }

    private File getFile(UUID playerId) {
        return new File(homesFolder, playerId + ".yml");
    }
}
