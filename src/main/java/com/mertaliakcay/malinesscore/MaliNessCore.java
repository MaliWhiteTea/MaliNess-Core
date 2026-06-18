package com.mertaliakcay.malinesscore;

import com.mertaliakcay.malinesscore.messages.MessageService;
import com.mertaliakcay.malinesscore.systems.SystemManager;
import com.mertaliakcay.malinesscore.systems.heal.HealSystem;
import com.mertaliakcay.malinesscore.util.PluginLang;
import org.bukkit.plugin.java.JavaPlugin;

public final class MaliNessCore extends JavaPlugin {

    private MessageService messageService;
    private PluginLang pluginLang;
    private SystemManager systemManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        messageService = new MessageService(this);
        messageService.reload();

        pluginLang = new PluginLang(this);

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

    /**
     * Yeni sistemler buraya eklenir.
     * Örnek: systemManager.register(new TpaSystem());
     */
    private void registerSystems() {
        systemManager.register(new HealSystem());
    }
}
