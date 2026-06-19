package com.mertaliakcay.malinesscore.systems.home;

import com.mertaliakcay.malinesscore.MaliNessCore;
import com.mertaliakcay.malinesscore.systems.home.model.HomeLocation;
import com.mertaliakcay.malinesscore.systems.home.model.PlayerHomes;
import com.mertaliakcay.malinesscore.util.SystemLang;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

public final class HomeStorage {

    private static final long DEFAULT_FLUSH_TIMEOUT_MS = 5_000L;

    private final MaliNessCore plugin;
    private final File homesFolder;
    private final HomeNameValidator nameValidator;
    private final SystemLang lang;
    private final Map<UUID, PlayerWriteState> writeStates = new ConcurrentHashMap<>();
    private final Object flushMonitor = new Object();

    public HomeStorage(MaliNessCore plugin, HomeNameValidator nameValidator, SystemLang lang) {
        this.plugin = plugin;
        this.nameValidator = nameValidator;
        this.lang = lang;
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
            HomeDataValidator.ValidationResult validation = HomeDataValidator.validate(name, homeSection, nameValidator);
            if (!validation.valid()) {
                String playerName = Bukkit.getOfflinePlayer(playerId).getName();
                if (playerName == null) {
                    playerName = playerId.toString();
                }
                notifyInvalidHomeSkipped(playerName, name, validation.reason());
                continue;
            }

            HomeLocation location = new HomeLocation(
                    validation.worldName(),
                    validation.x(),
                    validation.y(),
                    validation.z(),
                    validation.yaw(),
                    validation.pitch(),
                    validation.createdAt()
            );
            homes.put(validation.normalizedName(), location);
        }

        return homes;
    }

    public List<UUID> listStoredPlayerIds() {
        File[] files = homesFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            return List.of();
        }

        List<UUID> playerIds = new ArrayList<>();
        for (File file : files) {
            String fileName = file.getName();
            if (fileName.length() <= 4) {
                continue;
            }

            try {
                playerIds.add(UUID.fromString(fileName.substring(0, fileName.length() - 4)));
            } catch (IllegalArgumentException ignored) {
                lang.logInfo("storage-invalid-file-name", "file", fileName);
            }
        }

        return playerIds;
    }

    private void notifyInvalidHomeSkipped(String playerName, String homeName, String reason) {
        lang.logInfo("storage-invalid-home-skipped", "player", playerName, "home", homeName, "reason", reason);

        Component broadcast = lang.get("invalid-home-skipped-broadcast", "player", playerName, "home", homeName, "reason", reason);
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission(HomeSystem.PERM_INVALID_HOME_BROADCAST)) {
                online.sendMessage(broadcast);
            }
        }
    }

    public void saveAsync(UUID playerId, PlayerHomes homes) {
        PlayerHomes snapshot = PlayerHomes.copyOf(homes);
        PlayerWriteState state = writeStates.computeIfAbsent(playerId, ignored -> new PlayerWriteState());

        synchronized (state.lock) {
            state.pendingSave = snapshot;
            if (!state.drainRunning) {
                state.drainRunning = true;
                schedulePlayerDrain(playerId, state);
            }
        }
    }

    /**
     * Bekleyen tum ev yazimlarinin tamamlanmasini bekler.
     * Reload ve shutdown oncesinde cagrilmalidir.
     */
    public boolean flushAll() {
        return flushAll(DEFAULT_FLUSH_TIMEOUT_MS);
    }

    public boolean flushAll(long timeoutMs) {
        if (Bukkit.isPrimaryThread()) {
            try {
                return CompletableFuture.supplyAsync(() -> flushAllInternal(timeoutMs))
                        .get(timeoutMs, TimeUnit.MILLISECONDS);
            } catch (TimeoutException exception) {
                plugin.getLogger().warning("Ev kayit flush zaman asimina ugradi; kalan yazimlar senkron tamamlaniyor.");
                forceFlushAllPending();
                return false;
            } catch (Exception exception) {
                plugin.getLogger().log(Level.SEVERE, "Ev kayit flush basarisiz oldu.", exception);
                forceFlushAllPending();
                return false;
            }
        }

        return flushAllInternal(timeoutMs);
    }

    private boolean flushAllInternal(long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;

        while (true) {
            ensurePendingDrainsScheduled();

            if (!hasPendingWork()) {
                return true;
            }

            long remaining = deadline - System.currentTimeMillis();
            if (remaining <= 0) {
                forceFlushAllPending();
                return false;
            }

            if (awaitIdle(remaining)) {
                return true;
            }
        }
    }

    private void ensurePendingDrainsScheduled() {
        for (Map.Entry<UUID, PlayerWriteState> entry : writeStates.entrySet()) {
            UUID playerId = entry.getKey();
            PlayerWriteState state = entry.getValue();

            synchronized (state.lock) {
                if (state.pendingSave != null && !state.drainRunning) {
                    state.drainRunning = true;
                    schedulePlayerDrain(playerId, state);
                }
            }
        }
    }

    private boolean awaitIdle(long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;

        synchronized (flushMonitor) {
            while (hasPendingWork()) {
                long remaining = deadline - System.currentTimeMillis();
                if (remaining <= 0) {
                    return false;
                }

                try {
                    flushMonitor.wait(remaining);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }

        return true;
    }

    private boolean hasPendingWork() {
        for (PlayerWriteState state : writeStates.values()) {
            synchronized (state.lock) {
                if (state.pendingSave != null || state.drainRunning) {
                    return true;
                }
            }
        }
        return false;
    }

    private void forceFlushAllPending() {
        for (Map.Entry<UUID, PlayerWriteState> entry : writeStates.entrySet()) {
            UUID playerId = entry.getKey();
            PlayerWriteState state = entry.getValue();
            PlayerHomes snapshot;

            synchronized (state.lock) {
                long deadline = System.currentTimeMillis() + 1_000L;
                while (state.drainRunning && System.currentTimeMillis() < deadline) {
                    try {
                        state.lock.wait(Math.max(1L, deadline - System.currentTimeMillis()));
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }

                snapshot = state.pendingSave;
                state.pendingSave = null;
            }

            if (snapshot != null) {
                saveSync(playerId, snapshot);
            }
        }
    }

    private void schedulePlayerDrain(UUID playerId, PlayerWriteState state) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> drainPlayer(playerId, state));
    }

    private void drainPlayer(UUID playerId, PlayerWriteState state) {
        try {
            while (true) {
                PlayerHomes snapshot;

                synchronized (state.lock) {
                    snapshot = state.pendingSave;
                    if (snapshot == null) {
                        state.drainRunning = false;
                        if (state.pendingSave != null) {
                            state.drainRunning = true;
                            continue;
                        }
                        return;
                    }
                    state.pendingSave = null;
                }

                saveSync(playerId, snapshot);
            }
        } finally {
            notifyFlushMonitor();
        }
    }

    private void notifyFlushMonitor() {
        synchronized (flushMonitor) {
            flushMonitor.notifyAll();
        }
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

    private static final class PlayerWriteState {
        private final Object lock = new Object();
        private PlayerHomes pendingSave;
        private boolean drainRunning;
    }
}
