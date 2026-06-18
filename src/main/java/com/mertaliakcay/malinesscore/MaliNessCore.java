package com.mertaliakcay.malinesscore;

import com.mertaliakcay.malinesscore.command.MalinessCommand;
import com.mertaliakcay.malinesscore.messages.MessageService;
import com.mertaliakcay.malinesscore.systems.SystemManager;
import com.mertaliakcay.malinesscore.systems.feed.FeedSystem;
import com.mertaliakcay.malinesscore.systems.heal.HealSystem;
import com.mertaliakcay.malinesscore.systems.god.GodSystem;
import com.mertaliakcay.malinesscore.systems.health.HealthSystem;
import com.mertaliakcay.malinesscore.systems.hunger.HungerSystem;
import com.mertaliakcay.malinesscore.systems.saturate.SaturateSystem;
import com.mertaliakcay.malinesscore.systems.saturation.SaturationSystem;
import com.mertaliakcay.malinesscore.util.PluginLang;
import com.mertaliakcay.malinesscore.util.YamlMerger;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class MaliNessCore extends JavaPlugin {

    private MessageService messageService;
    private PluginLang pluginLang;
    private MalinessCommand malinessCommand;
    private SystemManager systemManager;

    @Override
    public void onEnable() {
        YamlMerger.mergeMainConfig(this);

        messageService = new MessageService(this);
        messageService.reload();

        pluginLang = new PluginLang(this);

        malinessCommand = new MalinessCommand(this);
        registerMalinessCommand();

        systemManager = new SystemManager(this);
        registerSystems();
        systemManager.enableAll();

        pluginLang.logInfo("plugin-enabled");
    }

    @Override
    public void onDisable() {
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

    private void registerMalinessCommand() {
        PluginCommand command = getCommand("maliness");
        if (command != null) {
            command.setExecutor(malinessCommand);
            command.setTabCompleter(malinessCommand);
        }
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
    }
}
