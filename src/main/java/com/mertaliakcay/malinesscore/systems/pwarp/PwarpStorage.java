package com.mertaliakcay.malinesscore.systems.pwarp;

import com.mertaliakcay.malinesscore.MaliNessCore;
import com.mertaliakcay.malinesscore.systems.pwarp.model.Pwarp;
import com.mertaliakcay.malinesscore.util.SystemLang;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PwarpStorage {

    private final MaliNessCore plugin;
    private final SystemLang lang;
    private final File file;
    private final Map<String, Pwarp> pwarpsByKey = new LinkedHashMap<>();
    private final Set<String> notifiedInvalidPwarps = ConcurrentHashMap.newKeySet();

    public PwarpStorage(MaliNessCore plugin, SystemLang lang) {
        this.plugin = plugin;
        this.lang = lang;
        this.file = new File(plugin.getDataFolder(), "data/pwarps.yml");
    }

    public void load() {
        pwarpsByKey.clear();
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            plugin.getLogger().warning("data klasörü oluşturulamadı.");
        }

        if (!file.exists()) {
            save();
            return;
        }

        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        List<Map<?, ?>> entries = configuration.getMapList("pwarps");
        for (Map<?, ?> entry : entries) {
            if (!(entry.get("name") instanceof String name)) {
                continue;
            }

            Pwarp pwarp = parsePwarp(name, entry);
            if (pwarp != null) {
                pwarpsByKey.put(PwarpNameValidator.canonicalKey(name), pwarp);
            }
        }
    }

    public void save() {
        YamlConfiguration configuration = new YamlConfiguration();
        List<Map<String, Object>> entries = new ArrayList<>();
        for (Pwarp pwarp : pwarpsByKey.values()) {
            entries.add(serialize(pwarp));
        }
        configuration.set("pwarps", entries);

        try {
            configuration.save(file);
        } catch (IOException exception) {
            plugin.getLogger().severe("pwarps.yml kaydedilemedi: " + exception.getMessage());
        }
    }

    public Collection<Pwarp> getAll() {
        return Collections.unmodifiableCollection(pwarpsByKey.values());
    }

    public List<Pwarp> getByOwner(UUID ownerId) {
        return pwarpsByKey.values().stream()
                .filter(pwarp -> pwarp.getOwnerId().equals(ownerId))
                .toList();
    }

    public int countByOwner(UUID ownerId) {
        return (int) pwarpsByKey.values().stream()
                .filter(pwarp -> pwarp.getOwnerId().equals(ownerId))
                .count();
    }

    public Pwarp find(String rawName) {
        return pwarpsByKey.get(PwarpNameValidator.canonicalKey(rawName));
    }

    public boolean contains(String rawName) {
        return pwarpsByKey.containsKey(PwarpNameValidator.canonicalKey(rawName));
    }

    public void put(Pwarp pwarp) {
        pwarpsByKey.put(PwarpNameValidator.canonicalKey(pwarp.getName()), pwarp);
        save();
    }

    public Pwarp remove(String rawName) {
        Pwarp removed = pwarpsByKey.remove(PwarpNameValidator.canonicalKey(rawName));
        if (removed != null) {
            save();
        }
        return removed;
    }

    public int size() {
        return pwarpsByKey.size();
    }

    public void validateWorlds() {
        for (Pwarp pwarp : pwarpsByKey.values()) {
            if (Bukkit.getWorld(pwarp.getWorldName()) == null) {
                broadcastInvalidPwarp(pwarp, "world-missing");
            }
        }
    }

    private void broadcastInvalidPwarp(Pwarp pwarp, String reason) {
        String key = PwarpNameValidator.canonicalKey(pwarp.getName()) + ":" + reason;
        if (!notifiedInvalidPwarps.add(key)) {
            return;
        }

        lang.logInfo("invalid-pwarp-skipped", "pwarp", pwarp.getName(), "reason", reason);
        Component broadcast = lang.get("invalid-pwarp-broadcast", "pwarp", pwarp.getName(), "reason", reason);
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission(PwarpSystem.PERM_INVALID_PWARP_BROADCAST)) {
                online.sendMessage(broadcast);
            }
        }
    }

    private Pwarp parsePwarp(String name, Map<?, ?> entry) {
        Object ownerIdRaw = entry.get("owner-id");
        Object ownerName = entry.get("owner-name");
        Object world = entry.get("world");
        Object x = entry.get("x");
        Object y = entry.get("y");
        Object z = entry.get("z");

        if (!(ownerIdRaw instanceof String ownerIdString)
                || !(world instanceof String worldName)
                || !(x instanceof Number xNum)
                || !(y instanceof Number yNum)
                || !(z instanceof Number zNum)) {
            return null;
        }

        UUID ownerId;
        try {
            ownerId = UUID.fromString(ownerIdString);
        } catch (IllegalArgumentException exception) {
            return null;
        }

        String ownerDisplay = ownerName instanceof String text ? text : "";
        String description = entry.get("description") instanceof String text ? text : "";
        float yaw = entry.get("yaw") instanceof Number yawNumber ? yawNumber.floatValue() : 0F;
        float pitch = entry.get("pitch") instanceof Number pitchNumber ? pitchNumber.floatValue() : 0F;
        long createdAt = entry.get("created-at") instanceof Number created ? created.longValue() : System.currentTimeMillis();
        long updatedAt = entry.get("updated-at") instanceof Number updated ? updated.longValue() : createdAt;
        long visitCount = entry.get("visit-count") instanceof Number visits ? visits.longValue() : 0L;
        long lastVisitedAt = entry.get("last-visited-at") instanceof Number lastVisit ? lastVisit.longValue() : 0L;

        return new Pwarp(
                name,
                ownerId,
                ownerDisplay,
                description,
                worldName,
                xNum.doubleValue(),
                yNum.doubleValue(),
                zNum.doubleValue(),
                yaw,
                pitch,
                createdAt,
                updatedAt,
                visitCount,
                lastVisitedAt
        );
    }

    private Map<String, Object> serialize(Pwarp pwarp) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", pwarp.getName());
        map.put("owner-id", pwarp.getOwnerId().toString());
        map.put("owner-name", pwarp.getOwnerName());
        map.put("description", pwarp.getDescription());
        map.put("world", pwarp.getWorldName());
        map.put("x", pwarp.getX());
        map.put("y", pwarp.getY());
        map.put("z", pwarp.getZ());
        map.put("yaw", pwarp.getYaw());
        map.put("pitch", pwarp.getPitch());
        map.put("created-at", pwarp.getCreatedAt());
        map.put("updated-at", pwarp.getUpdatedAt());
        map.put("visit-count", pwarp.getVisitCount());
        map.put("last-visited-at", pwarp.getLastVisitedAt());
        return map;
    }
}
