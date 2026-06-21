package com.mertaliakcay.malinesscore.systems.pwarp;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Set;

public final class PwarpSystem extends com.mertaliakcay.malinesscore.systems.AbstractGameSystem {

    public static final String PERM_USE = "maliness-core.pwarp.use";
    public static final String PERM_SET = "maliness-core.pwarp.set";
    public static final String PERM_DELETE = "maliness-core.pwarp.delete";
    public static final String PERM_LIST = "maliness-core.pwarp.list";
    public static final String PERM_EDIT = "maliness-core.pwarp.edit";
    public static final String PERM_MANAGE = "maliness-core.pwarp.manage";
    public static final String PERM_BYPASS_TIME = "maliness-core.pwarp.bypasstime";
    public static final String PERM_INVALID_PWARP_BROADCAST = "maliness-core.pwarp.invalid.broadcast";

    public static final List<String> SET_ALIASES = List.of("ekle", "set");
    public static final List<String> REMOVE_ALIASES = List.of("sil", "delete", "remove");
    public static final List<String> EDIT_ALIASES = List.of("düzenle", "edit");
    public static final List<String> LIST_ALIASES = List.of("list");

    public static final Set<String> EDIT_ACTIONS = Set.of("konum", "açıklama");
    public static final Set<String> LOCATION_ACTIONS = Set.of("konum");
    public static final Set<String> DESCRIPTION_ACTIONS = Set.of("açıklama");

    public static boolean bypassesPwarpRestrictions(Player player) {
        return player.isOp() || player.hasPermission(PERM_BYPASS_TIME);
    }

    private PwarpStorage storage;
    private PwarpService pwarpService;
    private PwarpCommand pwarpCommand;
    private PwarpMnCommand pwarpMnCommand;
    private PwarpRateLimiter rateLimiter;
    private BukkitTask rateLimitCleanupTask;

    @Override
    protected String getSystemId() {
        return "pwarp";
    }

    @Override
    protected void onRegister() {
        if (pwarpCommand == null) {
            pwarpCommand = new PwarpCommand(this);
        }

        registerLifecycleCommandsOnce(registrar -> {
            registrar.register(
                    "pwarp",
                    "Oyuncu warp noktalarına ışınlanır veya yönetir.",
                    List.of(),
                    new PwarpBasicCommand(pwarpCommand)
            );
            registrar.register(
                    "pwarps",
                    "Oyuncu warp noktalarını listeler.",
                    List.of(),
                    new PwarpsBasicCommand(pwarpCommand)
            );
        });
    }

    @Override
    protected void onEnable() {
        FileConfiguration configuration = config.get();
        PwarpNameValidator nameValidator = new PwarpNameValidator(
                configuration.getStringList("names.reserved"),
                configuration.getStringList("names.blacklist")
        );

        if (storage == null) {
            storage = new PwarpStorage(plugin, lang);
        }

        storage.load();
        storage.validateWorlds();

        PwarpLimitService limitService = new PwarpLimitService(
                configuration.getInt("limits.default-max-pwarps", 1),
                configuration.getInt("limits.max-count-permission-scan", 20)
        );

        rateLimiter = new PwarpRateLimiter(
                configuration.getInt("rate-limit.max-failures", 5),
                configuration.getLong("rate-limit.window-seconds", 30) * 1000L
        );

        PwarpLogger pwarpLogger = new PwarpLogger(
                plugin,
                configuration.getBoolean("logging.player-actions", true),
                configuration.getBoolean("logging.admin-actions", true)
        );

        pwarpService = new PwarpService(
                plugin,
                this,
                storage,
                nameValidator,
                limitService,
                rateLimiter,
                pwarpLogger,
                plugin.getTeleportService(),
                plugin.getConfirmationService()
        );

        pwarpService.reloadFromConfig();
        pwarpMnCommand = new PwarpMnCommand(pwarpCommand);
        plugin.getMalinessCommand().setPwarp(this, pwarpMnCommand);
    }

    @Override
    protected void onActivate() {
        rateLimitCleanupTask = plugin.getServer().getScheduler().runTaskTimerAsynchronously(
                plugin,
                () -> {
                    if (rateLimiter != null) {
                        rateLimiter.purgeExpiredEntries();
                    }
                },
                20L * 60 * 15,
                20L * 60 * 30
        );
    }

    @Override
    protected void onDeactivate() {
        if (rateLimitCleanupTask != null) {
            rateLimitCleanupTask.cancel();
            rateLimitCleanupTask = null;
        }

        plugin.getTeleportService().cancelAllWarmups();
    }

    @Override
    protected void onDisable() {
        onDeactivate();
        if (storage != null) {
            storage.save();
        }
    }

    @Override
    protected void onUnregister() {
        plugin.getMalinessCommand().clearPwarp();
    }

    public PwarpService getPwarpService() {
        return pwarpService;
    }

    public PwarpStorage getStorage() {
        return storage;
    }
}
