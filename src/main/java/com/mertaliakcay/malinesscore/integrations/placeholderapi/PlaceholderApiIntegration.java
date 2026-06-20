package com.mertaliakcay.malinesscore.integrations.placeholderapi;

import com.mertaliakcay.malinesscore.MaliNessCore;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

public final class PlaceholderApiIntegration implements Listener {

    private final MaliNessCore plugin;
    private final PlaceholderApiSettings settings = new PlaceholderApiSettings();
    private MaliNessPlaceholderResolver resolver;
    private MaliNessPlaceholderExpansion expansion;
    private boolean listenerRegistered;

    public PlaceholderApiIntegration(MaliNessCore plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        settings.reload(plugin);
        resolver = new MaliNessPlaceholderResolver(plugin, settings);
        registerExpansionIfNeeded();
        registerLateLoadListener();
    }

    public void reload() {
        settings.reload(plugin);
        if (resolver == null) {
            resolver = new MaliNessPlaceholderResolver(plugin, settings);
        }
        registerExpansionIfNeeded();
    }

    public void disable() {
        unregisterExpansion();
        unregisterLateLoadListener();
    }

    public boolean isAvailable() {
        return isPlaceholderApiPresent() && settings.isEnabled();
    }

    public boolean shouldParseInMessages() {
        return isAvailable() && settings.isParseInMessages();
    }

    public String applyPlaceholders(Player player, String text) {
        if (!shouldParseInMessages() || player == null || text == null || text.isEmpty()) {
            return text;
        }

        return PlaceholderAPI.setPlaceholders(player, text);
    }

    private void registerExpansionIfNeeded() {
        if (!isAvailable()) {
            unregisterExpansion();
            return;
        }

        if (expansion == null) {
            expansion = new MaliNessPlaceholderExpansion(plugin, resolver);
        }

        if (!expansion.isRegistered()) {
            expansion.register();
        }
    }

    private void unregisterExpansion() {
        if (expansion != null && expansion.isRegistered()) {
            expansion.unregister();
        }
    }

    private void registerLateLoadListener() {
        if (listenerRegistered) {
            return;
        }

        Bukkit.getPluginManager().registerEvents(this, plugin);
        listenerRegistered = true;
    }

    private void unregisterLateLoadListener() {
        if (!listenerRegistered) {
            return;
        }

        HandlerList.unregisterAll(this);
        listenerRegistered = false;
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        if (!"PlaceholderAPI".equalsIgnoreCase(event.getPlugin().getName())) {
            return;
        }

        registerExpansionIfNeeded();
    }

    private boolean isPlaceholderApiPresent() {
        Plugin placeholderApi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
        return placeholderApi != null && placeholderApi.isEnabled();
    }
}
