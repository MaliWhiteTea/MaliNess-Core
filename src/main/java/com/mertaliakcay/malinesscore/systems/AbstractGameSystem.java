package com.mertaliakcay.malinesscore.systems;

import com.mertaliakcay.malinesscore.MaliNessCore;
import com.mertaliakcay.malinesscore.util.SystemConfig;
import com.mertaliakcay.malinesscore.util.SystemLang;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.function.Consumer;

/**
 * Yeni sistemler bu sınıftan türetilir.
 * Otomatik olarak configs/&lt;sistemId&gt;.yml ve langs/&lt;sistemId&gt;.yml yüklenir.
 * Config'de {@code enabled: false} ise komut ve dinleyici kaydı yapılmaz.
 */
public abstract class AbstractGameSystem implements GameSystem {

    protected MaliNessCore plugin;
    protected SystemConfig config;
    protected SystemLang lang;

    private boolean active;
    private boolean lifecycleCommandsRegistered;
    private Listener registeredListener;

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

        onRegister();

        if (!isConfigEnabled()) {
            return;
        }

        active = true;
        onEnable();
    }

    @Override
    public final void disable() {
        unregisterListener();
        if (active) {
            onDisable();
            active = false;
        }
        onUnregister();
    }

    public final void reload() {
        unregisterListener();
        if (active) {
            onDisable();
            active = false;
        }

        config.reload();
        lang.reload();

        onRegister();

        if (!isConfigEnabled()) {
            onUnregister();
            return;
        }

        active = true;
        onEnable();
    }

    /**
     * Önceki listener varsa kaldırır, sonra yenisini kaydeder.
     * Reload'da birikme olmaması için kullanılmalıdır.
     */
    protected final void registerListener(Listener listener) {
        unregisterListener();
        registeredListener = listener;
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }

    protected final void unregisterListener() {
        if (registeredListener != null) {
            HandlerList.unregisterAll(registeredListener);
            registeredListener = null;
        }
    }

    protected final void registerLifecycleCommandsOnce(Consumer<Commands> registration) {
        if (lifecycleCommandsRegistered) {
            return;
        }

        lifecycleCommandsRegistered = true;
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> registration.accept(event.registrar()));
    }

    /**
     * Config ve lang yüklendikten sonra her zaman çağrılır.
     * Komut kaydı ve /mn entegrasyonu burada yapılmalıdır.
     */
    protected void onRegister() {
    }

    /**
     * Sistem yalnızca config'de enabled: true ise çağrılır.
     */
    protected abstract void onEnable();

    /**
     * Aktif sistem kapatılırken çağrılır.
     */
    protected abstract void onDisable();

    /**
     * Eklenti kapanırken her zaman çağrılır (aktif olsun ya da olmasın).
     */
    protected void onUnregister() {
    }

    public boolean isConfigEnabled() {
        return config != null && config.get().getBoolean("enabled", true);
    }

    public boolean isActive() {
        return active;
    }

    public boolean isEnabled() {
        return isActive();
    }

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
