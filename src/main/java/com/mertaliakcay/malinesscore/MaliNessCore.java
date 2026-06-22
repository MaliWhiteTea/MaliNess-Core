package com.mertaliakcay.malinesscore;

import com.mertaliakcay.malinesscore.command.MalinessCommand;
import com.mertaliakcay.malinesscore.confirmation.ConfirmCancelCommand;
import com.mertaliakcay.malinesscore.confirmation.ConfirmNoCommand;
import com.mertaliakcay.malinesscore.confirmation.ConfirmYesCommand;
import com.mertaliakcay.malinesscore.confirmation.ConfirmationListener;
import com.mertaliakcay.malinesscore.confirmation.ConfirmationService;
import com.mertaliakcay.malinesscore.integrations.placeholderapi.PlaceholderApiIntegration;
import com.mertaliakcay.malinesscore.messages.MessageService;
import com.mertaliakcay.malinesscore.systems.SystemManager;
import com.mertaliakcay.malinesscore.systems.control.NonClosableSystemRegistry;
import com.mertaliakcay.malinesscore.systems.control.SystemBasicCommand;
import com.mertaliakcay.malinesscore.systems.control.SystemControlService;
import com.mertaliakcay.malinesscore.systems.control.SystemDependencyRegistry;
import com.mertaliakcay.malinesscore.systems.control.SystemMnCommand;
import com.mertaliakcay.malinesscore.systems.control.SystemsAuditLogger;
import com.mertaliakcay.malinesscore.systems.control.SystemsBasicCommand;
import com.mertaliakcay.malinesscore.systems.feed.FeedSystem;
import com.mertaliakcay.malinesscore.systems.god.GodSystem;
import com.mertaliakcay.malinesscore.systems.heal.HealSystem;
import com.mertaliakcay.malinesscore.systems.health.HealthSystem;
import com.mertaliakcay.malinesscore.systems.home.HomeSystem;
import com.mertaliakcay.malinesscore.teleport.TeleportListener;
import com.mertaliakcay.malinesscore.teleport.TeleportRestrictions;
import com.mertaliakcay.malinesscore.teleport.TeleportService;
import com.mertaliakcay.malinesscore.systems.hunger.HungerSystem;
import com.mertaliakcay.malinesscore.systems.saturate.SaturateSystem;
import com.mertaliakcay.malinesscore.systems.saturation.SaturationSystem;
import com.mertaliakcay.malinesscore.systems.playtime.PlaytimeSystem;
import com.mertaliakcay.malinesscore.systems.broadcast.BroadcastSystem;
import com.mertaliakcay.malinesscore.systems.vanish.VanishService;
import com.mertaliakcay.malinesscore.systems.vanish.VanishSystem;
import com.mertaliakcay.malinesscore.gui.MenuService;
import com.mertaliakcay.malinesscore.systems.pwarp.PwarpSystem;
import com.mertaliakcay.malinesscore.systems.gui.GuiSystem;
import com.mertaliakcay.malinesscore.systems.warp.WarpSystem;
import com.mertaliakcay.malinesscore.util.PluginLang;
import com.mertaliakcay.malinesscore.util.YamlMerger;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class MaliNessCore extends JavaPlugin {

    private MessageService messageService;
    private PlaceholderApiIntegration placeholderApiIntegration;
    private PluginLang pluginLang;
    private MalinessCommand malinessCommand;
    private SystemManager systemManager;
    private ConfirmationService confirmationService;
    private TeleportService teleportService;
    private SystemControlService systemControlService;
    private SystemMnCommand systemMnCommand;
    private VanishService vanishService;
    private GuiSystem guiSystem;
    private volatile boolean reloading;
    private boolean globalCommandsRegistered;
    private boolean systemCommandsRegistered;

    @Override
    public void onEnable() {
        YamlMerger.mergeMainConfig(this);

        messageService = new MessageService(this);
        messageService.reload();

        placeholderApiIntegration = new PlaceholderApiIntegration(this);

        pluginLang = new PluginLang(this);

        confirmationService = new ConfirmationService(this);
        teleportService = new TeleportService(this);

        registerGlobalCommands();
        getServer().getPluginManager().registerEvents(new ConfirmationListener(this), this);
        getServer().getPluginManager().registerEvents(
                new TeleportListener(teleportService, TeleportRestrictions::bypassesVehicleRestrictions),
                this
        );

        malinessCommand = new MalinessCommand(this);
        registerMalinessCommand();

        systemManager = new SystemManager(this);
        registerSystems();
        systemManager.enableAll();
        registerSystemControlCommands();
        placeholderApiIntegration.enable();

        pluginLang.logInfo("plugin-enabled");
    }

    @Override
    public void onDisable() {
        if (placeholderApiIntegration != null) {
            placeholderApiIntegration.disable();
        }
        if (confirmationService != null) {
            confirmationService.cancelAll();
        }
        if (teleportService != null) {
            teleportService.cancelAllWarmups();
        }
        if (systemManager != null) {
            systemManager.disableAll();
        }

        if (pluginLang != null) {
            pluginLang.logInfo("plugin-disabled");
        }
    }

    public MessageService getMessageService() {
        return messageService;
    }

    public PlaceholderApiIntegration getPlaceholderApiIntegration() {
        return placeholderApiIntegration;
    }

    public PluginLang getPluginLang() {
        return pluginLang;
    }

    public SystemManager getSystemManager() {
        return systemManager;
    }

    public MalinessCommand getMalinessCommand() {
        return malinessCommand;
    }

    public ConfirmationService getConfirmationService() {
        return confirmationService;
    }

    public TeleportService getTeleportService() {
        return teleportService;
    }

    /** @deprecated use {@link #getTeleportService()} */
    @Deprecated
    public TeleportService getHomeTeleportManager() {
        return teleportService;
    }

    public SystemControlService getSystemControlService() {
        return systemControlService;
    }

    public SystemMnCommand getSystemMnCommand() {
        return systemMnCommand;
    }

    public VanishService getVanishService() {
        return vanishService;
    }

    public GuiSystem getGuiSystem() {
        return guiSystem;
    }

    public MenuService getMenuService() {
        return guiSystem != null ? guiSystem.getMenuService() : null;
    }

    public void setVanishService(VanishService vanishService) {
        this.vanishService = vanishService;
    }

    public void reloadPlugin(CommandSender sender) {
        reloading = true;
        try {
            YamlMerger.mergeMainConfig(this);
            messageService.reload();
            pluginLang.reload();
            confirmationService.cancelAll();
            teleportService.cancelAllWarmups();
            systemManager.reloadAll();
            if (systemControlService != null) {
                systemControlService.refreshCatalog();
            }
            if (placeholderApiIntegration != null) {
                placeholderApiIntegration.reload();
            }

            if (sender == null) {
                pluginLang.logInfo("reload-success");
            } else {
                pluginLang.send(sender, "reload-success");
            }
        } finally {
            reloading = false;
        }
    }

    public boolean isReloading() {
        return reloading;
    }

    private void registerMalinessCommand() {
        PluginCommand command = getCommand("maliness");
        if (command != null) {
            command.setExecutor(malinessCommand);
            command.setTabCompleter(malinessCommand);
        }
    }

    private void registerGlobalCommands() {
        if (globalCommandsRegistered) {
            return;
        }
        globalCommandsRegistered = true;

        ConfirmYesCommand yesCommand = new ConfirmYesCommand(this);
        ConfirmNoCommand noCommand = new ConfirmNoCommand(this);
        ConfirmCancelCommand cancelCommand = new ConfirmCancelCommand(this);

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            event.registrar().register("evet", "Bekleyen onayı kabul eder.", List.of(), yesCommand);
            event.registrar().register("hayir", "Bekleyen onayı reddeder.", List.of("hayır"), noCommand);
            event.registrar().register("iptal", "Bekleyen işlemi iptal eder.", List.of(), cancelCommand);
        });
    }

    /**
     * Yeni sistemler buraya eklenir.
     * Örnek: systemManager.register(new TpaSystem());
     */
    private void registerSystems() {
        systemManager.register(new HealSystem());
        systemManager.register(new FeedSystem());
        systemManager.register(new HealthSystem());
        systemManager.register(new HungerSystem());
        systemManager.register(new SaturateSystem());
        systemManager.register(new SaturationSystem());
        systemManager.register(new GodSystem());
        systemManager.register(new HomeSystem());
        systemManager.register(new PlaytimeSystem());
        systemManager.register(new BroadcastSystem());
        systemManager.register(new VanishSystem());
        systemManager.register(new WarpSystem());
        systemManager.register(new PwarpSystem());
        guiSystem = new GuiSystem();
        systemManager.register(guiSystem);
    }

    private void registerSystemControlCommands() {
        if (systemCommandsRegistered) {
            return;
        }
        systemCommandsRegistered = true;

        NonClosableSystemRegistry nonClosableRegistry = new NonClosableSystemRegistry();
        SystemDependencyRegistry dependencyRegistry = new SystemDependencyRegistry();
        SystemsAuditLogger auditLogger = new SystemsAuditLogger(this);

        systemControlService = new SystemControlService(
                this,
                systemManager,
                nonClosableRegistry,
                dependencyRegistry,
                auditLogger
        );
        systemMnCommand = new SystemMnCommand(this, systemControlService);
        malinessCommand.setSystemControl(systemMnCommand, systemControlService);

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            event.registrar().register(
                    "systems",
                    "Oyun sistemlerini listeler.",
                    List.of(SystemControlService.ALIAS_SYSTEMS_TR),
                    new SystemsBasicCommand(this, systemControlService)
            );
            event.registrar().register(
                    "system",
                    "Oyun sistemini açar, kapatır veya bilgi gösterir.",
                    List.of(SystemControlService.ALIAS_SYSTEM_TR),
                    new SystemBasicCommand(this, systemControlService)
            );
        });
    }
}
