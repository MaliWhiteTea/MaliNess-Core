package com.mertaliakcay.malinesscore.systems.warp;

import com.mertaliakcay.malinesscore.MaliNessCore;
import com.mertaliakcay.malinesscore.systems.warp.model.Warp;
import com.mertaliakcay.malinesscore.util.SystemLang;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class WarpStorage {

    private final MaliNessCore plugin;
    private final SystemLang lang;
    private final File file;
    private final Map<String, Warp> warpsByKey = new LinkedHashMap<>();
    private final Set<String> notifiedInvalidWarps = ConcurrentHashMap.newKeySet();

    public WarpStorage(MaliNessCore plugin, SystemLang lang) {
        this.plugin = plugin;
        this.lang = lang;
        this.file = new File(plugin.getDataFolder(), "data/warps.yml");
    }

    public void load() {
        warpsByKey.clear();
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            plugin.getLogger().warning("data klasörü oluşturulamadı.");
        }

        if (!file.exists()) {
            save();
            return;
        }

        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        List<Map<?, ?>> entries = configuration.getMapList("warps");
        for (Map<?, ?> entry : entries) {
            if (!(entry.get("name") instanceof String name)) {
                continue;
            }

            Warp warp = parseWarp(name, entry);
            if (warp != null) {
                warpsByKey.put(WarpNameValidator.canonicalKey(name), warp);
            }
        }
    }

    public void save() {
        YamlConfiguration configuration = new YamlConfiguration();
        List<Map<String, Object>> entries = new ArrayList<>();
        for (Warp warp : warpsByKey.values()) {
            entries.add(serialize(warp));
        }
        configuration.set("warps", entries);

        try {
            configuration.save(file);
        } catch (IOException exception) {
            plugin.getLogger().severe("warps.yml kaydedilemedi: " + exception.getMessage());
        }
    }

    public Collection<Warp> getAll() {
        return Collections.unmodifiableCollection(warpsByKey.values());
    }

    public Warp find(String rawName) {
        return warpsByKey.get(WarpNameValidator.canonicalKey(rawName));
    }

    public boolean contains(String rawName) {
        return warpsByKey.containsKey(WarpNameValidator.canonicalKey(rawName));
    }

    public void put(Warp warp) {
        warpsByKey.put(WarpNameValidator.canonicalKey(warp.getName()), warp);
        save();
    }

    public Warp remove(String rawName) {
        Warp removed = warpsByKey.remove(WarpNameValidator.canonicalKey(rawName));
        if (removed != null) {
            save();
        }
        return removed;
    }

    public int size() {
        return warpsByKey.size();
    }

    public void validateWorlds() {
        for (Warp warp : warpsByKey.values()) {
            if (Bukkit.getWorld(warp.getWorldName()) == null) {
                broadcastInvalidWarp(warp, "world-missing");
            }
        }
    }

    private void broadcastInvalidWarp(Warp warp, String reason) {
        String key = WarpNameValidator.canonicalKey(warp.getName()) + ":" + reason;
        if (!notifiedInvalidWarps.add(key)) {
            return;
        }

        lang.logInfo("invalid-warp-skipped", "warp", warp.getName(), "reason", reason);
        Component broadcast = lang.get("invalid-warp-broadcast", "warp", warp.getName(), "reason", reason);
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission(WarpSystem.PERM_INVALID_WARP_BROADCAST)) {
                online.sendMessage(broadcast);
            }
        }
    }

    private Warp parseWarp(String name, Map<?, ?> entry) {
        Object world = entry.get("world");
        Object x = entry.get("x");
        Object y = entry.get("y");
        Object z = entry.get("z");
        if (!(world instanceof String worldName) || !(x instanceof Number xNum) || !(y instanceof Number yNum) || !(z instanceof Number zNum)) {
            return null;
        }

        boolean enabled = entry.containsKey("enabled") ? Boolean.TRUE.equals(entry.get("enabled")) : true;
        String description = entry.get("description") instanceof String text ? text : "";
        float yaw = entry.get("yaw") instanceof Number yawNumber ? yawNumber.floatValue() : 0F;
        float pitch = entry.get("pitch") instanceof Number pitchNumber ? pitchNumber.floatValue() : 0F;
        long createdAt = entry.get("created-at") instanceof Number created ? created.longValue() : System.currentTimeMillis();
        long updatedAt = entry.get("updated-at") instanceof Number updated ? updated.longValue() : createdAt;

        return new Warp(
                name,
                enabled,
                description,
                worldName,
                xNum.doubleValue(),
                yNum.doubleValue(),
                zNum.doubleValue(),
                yaw,
                pitch,
                createdAt,
                updatedAt
        );
    }

    private Map<String, Object> serialize(Warp warp) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", warp.getName());
        map.put("enabled", warp.isEnabled());
        map.put("description", warp.getDescription());
        map.put("world", warp.getWorldName());
        map.put("x", warp.getX());
        map.put("y", warp.getY());
        map.put("z", warp.getZ());
        map.put("yaw", warp.getYaw());
        map.put("pitch", warp.getPitch());
        map.put("created-at", warp.getCreatedAt());
        map.put("updated-at", warp.getUpdatedAt());
        return map;
    }
}
