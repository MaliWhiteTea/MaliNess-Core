package com.mertaliakcay.malinesscore.systems.playtime;

import com.mertaliakcay.malinesscore.MaliNessCore;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class PlaytimeService {

    private final MaliNessCore plugin;
    private final PlaytimeStorage storage;
    private final PlaytimeTracker tracker = new PlaytimeTracker();
    private PlaytimeFormatConfig formatConfig;

    public PlaytimeService(MaliNessCore plugin, PlaytimeStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    public void reload(ConfigurationSection formatSection) {
        this.formatConfig = new PlaytimeFormatConfig(formatSection);
    }

    public void startSession(Player player) {
        tracker.startSession(player.getUniqueId());
    }

    public void endSession(Player player) {
        UUID playerId = player.getUniqueId();
        if (!tracker.hasSession(playerId)) {
            return;
        }

        long sessionMillis = tracker.collectSessionMillis(playerId);
        storage.setTotalMillis(playerId, storage.getTotalMillis(playerId) + sessionMillis);
        storage.save(playerId);
        tracker.endSession(playerId);
    }

    public void flushSession(Player player) {
        UUID playerId = player.getUniqueId();
        if (!tracker.hasSession(playerId)) {
            return;
        }

        long sessionMillis = tracker.collectSessionMillis(playerId);
        storage.setTotalMillis(playerId, storage.getTotalMillis(playerId) + sessionMillis);
        storage.save(playerId);
        tracker.startSession(playerId);
    }

    public void flushAllOnline() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            flushSession(player);
        }
    }

    public void pauseSession(UUID playerId) {
        tracker.pauseSession(playerId);
    }

    public void resumeSession(UUID playerId) {
        tracker.resumeSession(playerId);
    }

    public long getTotalSeconds(UUID playerId) {
        long totalMillis = storage.getTotalMillis(playerId);
        if (tracker.hasSession(playerId)) {
            totalMillis += tracker.collectSessionMillis(playerId);
        }
        return totalMillis / 1000L;
    }

    public long getTotalSeconds(OfflinePlayer player) {
        if (player == null) {
            return 0L;
        }

        Player online = player.getPlayer();
        if (online != null && online.isOnline()) {
            return getTotalSeconds(online.getUniqueId());
        }

        return storage.getTotalMillis(player.getUniqueId()) / 1000L;
    }

    public String getFormatted(UUID playerId) {
        return formatConfig.format(getTotalSeconds(playerId));
    }

    public String getFormatted(OfflinePlayer player) {
        return formatConfig.format(getTotalSeconds(player));
    }

    public void shutdown() {
        flushAllOnline();
        storage.flushAll();
    }
}
