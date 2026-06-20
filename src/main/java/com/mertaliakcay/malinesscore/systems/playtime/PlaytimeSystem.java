package com.mertaliakcay.malinesscore.systems.playtime;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public final class PlaytimeSystem extends com.mertaliakcay.malinesscore.systems.AbstractGameSystem {

    public static final String PERM_USE = "maliness-core.playtime.use";
    public static final String PERM_OTHERS = "maliness-core.playtime.use.others";
    public static final String ALIAS_TURKISH = "oynamasüresi";

    private PlaytimeStorage storage;
    private PlaytimeService playtimeService;
    private PlaytimeCommand playtimeCommand;
    private PlaytimeMnCommand playtimeMnCommand;
    private BukkitTask flushTask;

    @Override
    protected String getSystemId() {
        return "playtime";
    }

    @Override
    protected void onRegister() {
        if (playtimeCommand == null) {
            playtimeCommand = new PlaytimeCommand(this);
        }

        registerLifecycleCommandsOnce(registrar -> registrar.register(
                "playtime",
                "Oynama süresini gösterir.",
                List.of(ALIAS_TURKISH),
                new PlaytimeBasicCommand(playtimeCommand)
        ));

        playtimeMnCommand = new PlaytimeMnCommand(playtimeCommand);
        plugin.getMalinessCommand().setPlaytime(this, playtimeMnCommand);
    }

    @Override
    protected void onEnable() {
        if (storage == null) {
            storage = new PlaytimeStorage(plugin);
        }

        if (playtimeService == null) {
            playtimeService = new PlaytimeService(plugin, storage);
        }

        playtimeService.reload(config.get().getConfigurationSection("format"));

        for (Player player : Bukkit.getOnlinePlayers()) {
            playtimeService.startSession(player);
        }
    }

    @Override
    protected void onActivate() {
        registerListener(new PlaytimeListener(playtimeService));

        int flushIntervalSeconds = Math.max(30, config.get().getInt("tracking.flush-interval-seconds", 300));
        flushTask = plugin.getServer().getScheduler().runTaskTimer(
                plugin,
                () -> playtimeService.flushAllOnline(),
                flushIntervalSeconds * 20L,
                flushIntervalSeconds * 20L
        );
    }

    @Override
    protected void onDeactivate() {
        if (flushTask != null) {
            flushTask.cancel();
            flushTask = null;
        }
    }

    @Override
    protected void onDisable() {
        onDeactivate();
        if (playtimeService != null) {
            playtimeService.shutdown();
        }
    }

    @Override
    protected void onUnregister() {
        plugin.getMalinessCommand().clearPlaytime();
    }

    public PlaytimeService getPlaytimeService() {
        return playtimeService;
    }
}
