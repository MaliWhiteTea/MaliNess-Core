package com.mertaliakcay.malinesscore.systems.home;

import com.mertaliakcay.malinesscore.systems.AbstractGameSystem;
import com.mertaliakcay.malinesscore.systems.home.HomeMnCommand;
import com.mertaliakcay.malinesscore.systems.home.commands.DelHomeCommand;
import com.mertaliakcay.malinesscore.systems.home.commands.HomeCommand;
import com.mertaliakcay.malinesscore.systems.home.commands.HomesCommand;
import com.mertaliakcay.malinesscore.systems.home.commands.RenameHomeCommand;
import com.mertaliakcay.malinesscore.systems.home.commands.SetHomeCommand;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.configuration.file.FileConfiguration;

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

    public static final String ALIAS_SETHOME = "evayarla";
    public static final String ALIAS_HOME_1 = "house";
    public static final String ALIAS_HOME_2 = "ev";
    public static final String ALIAS_RENAME_1 = "evadıdeğiştir";
    public static final String ALIAS_RENAME_2 = "evismideğiştir";

    private HomeStorage storage;
    private HomeService homeService;
    private HomeListener homeListener;
    private HomeMnCommand homeMnCommand;

    @Override
    protected String getSystemId() {
        return "home";
    }

    @Override
    protected void onEnable() {
        if (!config.get().getBoolean("enabled", true)) {
            return;
        }

        FileConfiguration configuration = config.get();
        storage = new HomeStorage(plugin);
        HomeLimitService limitService = new HomeLimitService(
                configuration.getInt("limits.default-max-homes", 1),
                configuration.getInt("limits.max-count-permission-scan", 20)
        );
        HomeNameValidator nameValidator = new HomeNameValidator(
                configuration.getStringList("names.reserved"),
                configuration.getString("names.default-name", "ev")
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
        homeService.reloadFromConfig();

        homeListener = new HomeListener(teleportManager);
        plugin.getServer().getPluginManager().registerEvents(homeListener, plugin);

        homeMnCommand = new HomeMnCommand(homeService);
        plugin.getMalinessCommand().setHome(this, homeMnCommand);

        SetHomeCommand setHomeCommand = new SetHomeCommand(homeService);
        HomeCommand homeCommand = new HomeCommand(homeService);
        DelHomeCommand delHomeCommand = new DelHomeCommand(homeService);
        HomesCommand homesCommand = new HomesCommand(homeService);
        RenameHomeCommand renameHomeCommand = new RenameHomeCommand(homeService);

        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            event.registrar().register("sethome", "Ev kaydeder.", List.of(ALIAS_SETHOME), setHomeCommand);
            event.registrar().register("home", "Eve ışınlanır.", List.of(ALIAS_HOME_1, ALIAS_HOME_2), homeCommand);
            event.registrar().register("delhome", "Evi siler.", List.of("remhome"), delHomeCommand);
            event.registrar().register("homes", "Evleri listeler.", List.of(), homesCommand);
            event.registrar().register("renamehome", "Ev adını değiştirir.", List.of(ALIAS_RENAME_1, ALIAS_RENAME_2), renameHomeCommand);
        });
    }

    @Override
    protected void onDisable() {
        plugin.getMalinessCommand().clearHome();
        homeService = null;
        homeListener = null;
        homeMnCommand = null;
        storage = null;
    }

    public boolean isEnabled() {
        return config.get().getBoolean("enabled", true);
    }
}
