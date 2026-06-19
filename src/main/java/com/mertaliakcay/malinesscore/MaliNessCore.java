package com.mertaliakcay.malinesscore;

import com.mertaliakcay.malinesscore.command.MalinessCommand;
import com.mertaliakcay.malinesscore.confirmation.ConfirmCancelCommand;
import com.mertaliakcay.malinesscore.confirmation.ConfirmNoCommand;
import com.mertaliakcay.malinesscore.confirmation.ConfirmYesCommand;
import com.mertaliakcay.malinesscore.confirmation.ConfirmationListener;
import com.mertaliakcay.malinesscore.confirmation.ConfirmationService;
import com.mertaliakcay.malinesscore.messages.MessageService;
import com.mertaliakcay.malinesscore.systems.SystemManager;
import com.mertaliakcay.malinesscore.systems.feed.FeedSystem;
import com.mertaliakcay.malinesscore.systems.god.GodSystem;
import com.mertaliakcay.malinesscore.systems.heal.HealSystem;
import com.mertaliakcay.malinesscore.systems.health.HealthSystem;
import com.mertaliakcay.malinesscore.systems.home.HomeSystem;
import com.mertaliakcay.malinesscore.systems.home.HomeTeleportManager;
import com.mertaliakcay.malinesscore.systems.hunger.HungerSystem;
import com.mertaliakcay.malinesscore.systems.saturate.SaturateSystem;
import com.mertaliakcay.malinesscore.systems.saturation.SaturationSystem;
import com.mertaliakcay.malinesscore.util.PluginLang;
import com.mertaliakcay.malinesscore.util.YamlMerger;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class MaliNessCore extends JavaPlugin {

    private MessageService messageService;
    private PluginLang pluginLang;
    private MalinessCommand malinessCommand;
    private SystemManager systemManager;
    private ConfirmationService confirmationService;
    private HomeTeleportManager homeTeleportManager;
    private volatile boolean reloading;
    private boolean globalCommandsRegistered;

    @Override
    public void onEnable() {
        YamlMerger.mergeMainConfig(this);

        messageService = new MessageService(this);
        messageService.reload();

        pluginLang = new PluginLang(this);

        confirmationService = new ConfirmationService(this);
        homeTeleportManager = new HomeTeleportManager(this);

        registerGlobalCommands();
        getServer().getPluginManager().registerEvents(new ConfirmationListener(this), this);

        malinessCommand = new MalinessCommand(this);
        registerMalinessCommand();

        systemManager = new SystemManager(this);
        registerSystems();
        systemManager.enableAll();

        pluginLang.logInfo("plugin-enabled");
    }

    @Override
    public void onDisable() {
        if (confirmationService != null) {
            confirmationService.cancelAll();
        }
        if (homeTeleportManager != null) {
            homeTeleportManager.cancelAllWarmups();
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

    public HomeTeleportManager getHomeTeleportManager() {
        return homeTeleportManager;
    }

    public void reloadPlugin(CommandSender sender) {
        reloading = true;
        try {
            YamlMerger.mergeMainConfig(this);
            messageService.reload();
            pluginLang.reload();
            confirmationService.cancelAll();
            homeTeleportManager.cancelAllWarmups();
            systemManager.reloadAll();

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
    }
}
