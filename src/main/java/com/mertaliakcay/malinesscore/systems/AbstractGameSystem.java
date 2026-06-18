package com.mertaliakcay.malinesscore.systems;

import com.mertaliakcay.malinesscore.MaliNessCore;
import com.mertaliakcay.malinesscore.util.SystemConfig;
import com.mertaliakcay.malinesscore.util.SystemLang;

/**
 * Yeni sistemler bu sınıftan türetilir.
 * Otomatik olarak configs/&lt;sistemId&gt;.yml ve langs/&lt;sistemId&gt;.yml yüklenir.
 */
public abstract class AbstractGameSystem implements GameSystem {

    protected MaliNessCore plugin;
    protected SystemConfig config;
    protected SystemLang lang;

    protected abstract String getSystemId();

    @Override
    public final String getName() {
        return getSystemId();
    }

    @Override
    public final void enable(MaliNessCore plugin) {
        this.plugin = plugin;
        this.config = new SystemConfig(plugin, getSystemId());
        this.lang = new SystemLang(plugin, getSystemId());
        onEnable();
    }

    @Override
    public final void disable() {
        onDisable();
    }

    protected abstract void onEnable();

    protected abstract void onDisable();

    public SystemLang getLang() {
        return lang;
    }

    public SystemConfig getConfig() {
        return config;
    }

    public MaliNessCore getPlugin() {
        return plugin;
    }
}
