package com.mertaliakcay.malinesscore.systems.home;

import com.mertaliakcay.malinesscore.systems.AbstractGameSystem;
import com.mertaliakcay.malinesscore.systems.home.commands.DelHomeCommand;
import com.mertaliakcay.malinesscore.systems.home.commands.HomeCommand;
import com.mertaliakcay.malinesscore.systems.home.commands.HomesCommand;
import com.mertaliakcay.malinesscore.systems.home.commands.RenameHomeCommand;
import com.mertaliakcay.malinesscore.systems.home.commands.SetHomeCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public final class HomeSystem extends AbstractGameSystem {

    public static final String PERM_USE = "maliness-core.home.use";
    public static final String PERM_SETHOME = "maliness-core.home.sethome";
    public static final String PERM_DELHOME = "maliness-core.home.delhome";
    public static final String PERM_HOMES = "maliness-core.home.homes";
    public static final String PERM_RENAME = "maliness-core.home.rename";
    public static final String PERM_BYPASS_TIME = "maliness-core.home.bypasstime";
    public static final String PERM_OTHERS_LIST = "maliness-core.home.others.list";
    public static final String PERM_OTHERS_TELEPORT = "maliness-core.home.others.teleport";
    public static final String PERM_OTHERS_DELETE = "maliness-core.home.others.delete";
    public static final String PERM_INVALID_HOME_BROADCAST = "maliness-core.home.invalid-home.broadcast";

    public static final String ALIAS_SETHOME = "evayarla";
    public static final String ALIAS_HOME_1 = "house";
    public static final String ALIAS_HOME_2 = "ev";
    public static final String ALIAS_RENAME_1 = "evadıdeğiştir";
    public static final String ALIAS_RENAME_2 = "evismideğiştir";
    public static final String ALIAS_DELHOME = "evsil";

    public static boolean bypassesHomeRestrictions(Player player) {
        return player.isOp() || player.hasPermission(PERM_BYPASS_TIME);
    }

    private HomeStorage storage;
    private HomeService homeService;
    private HomeMnCommand homeMnCommand;
    private HomeListener homeListener;
    private BukkitTask cacheCleanupTask;

    @Override
    protected String getSystemId() {
        return "home";
    }

    @Override
    protected void onRegister() {
        registerLifecycleCommandsOnce(registrar -> {
            registrar.register("sethome", "Ev kaydeder.", List.of(ALIAS_SETHOME), new SetHomeCommand(this));
            registrar.register("home", "Eve ışınlanır.", List.of(ALIAS_HOME_1, ALIAS_HOME_2), new HomeCommand(this));
            registrar.register("delhome", "Evi siler.", List.of("remhome", ALIAS_DELHOME), new DelHomeCommand(this));
            registrar.register("homes", "Evleri listeler.", List.of(), new HomesCommand(this));
            registrar.register("renamehome", "Ev adını değiştirir.", List.of(ALIAS_RENAME_1, ALIAS_RENAME_2), new RenameHomeCommand(this));
        });

        plugin.getMalinessCommand().setHome(this, homeMnCommand);
    }

    @Override
    protected void onEnable() {
        FileConfiguration configuration = config.get();
        HomeNameValidator nameValidator = new HomeNameValidator(
                configuration.getStringList("names.reserved"),
                configuration.getString("names.default-name", "ev")
        );

        if (storage != null) {
            storage.updateNameValidator(nameValidator);
        } else {
            storage = new HomeStorage(plugin, nameValidator, lang);
        }

        HomeLimitService limitService = new HomeLimitService(
                configuration.getInt("limits.default-max-homes", 1),
                configuration.getInt("limits.max-count-permission-scan", 20)
        );
        HomeLogger homeLogger = new HomeLogger(
                plugin,
                configuration.getBoolean("logging.player-actions", true),
                configuration.getBoolean("logging.admin-actions", true)
        );
        HomeRateLimiter rateLimiter = new HomeRateLimiter(
                configuration.getInt("rate-limit.max-failures", 5),
                configuration.getLong("rate-limit.window-seconds", 30) * 1000L
        );

        HomeTeleportManager teleportManager = plugin.getHomeTeleportManager();
        teleportManager.configure(
                configuration.getInt("teleport.warmup-seconds", 5),
                configuration.getInt("teleport.fire-resistance-seconds", 3),
                lang
        );

        if (homeService == null) {
            homeService = new HomeService(
                    plugin,
                    this,
                    storage,
                    limitService,
                    nameValidator,
                    homeLogger,
                    rateLimiter,
                    teleportManager,
                    plugin.getConfirmationService()
            );
        } else {
            homeService = new HomeService(
                    plugin,
                    this,
                    storage,
                    limitService,
                    nameValidator,
                    homeLogger,
                    rateLimiter,
                    teleportManager,
                    plugin.getConfirmationService()
            );
        }

        homeService.reloadFromConfig();
        homeMnCommand = new HomeMnCommand(homeService);
        plugin.getMalinessCommand().setHome(this, homeMnCommand);
    }

    @Override
    protected void onActivate() {
        HomeTeleportManager teleportManager = plugin.getHomeTeleportManager();
        homeListener = new HomeListener(teleportManager, homeService);
        registerListener(homeListener);

        cacheCleanupTask = plugin.getServer().getScheduler().runTaskTimerAsynchronously(
                plugin,
                () -> {
                    homeService.purgeExpiredCaches();
                    homeService.purgeInactiveLocks();
                },
                20L * 60 * 15,
                20L * 60 * 30
        );
    }

    @Override
    protected void onDeactivate() {
        if (cacheCleanupTask != null) {
            cacheCleanupTask.cancel();
            cacheCleanupTask = null;
        }

        plugin.getHomeTeleportManager().cancelAllWarmups();
    }

    @Override
    protected void onDisable() {
        onDeactivate();
        if (storage != null) {
            storage.flushAll();
        }
    }

    @Override
    protected void onUnregister() {
        plugin.getMalinessCommand().clearHome();
    }

    public HomeService getHomeService() {
        return homeService;
    }
}
