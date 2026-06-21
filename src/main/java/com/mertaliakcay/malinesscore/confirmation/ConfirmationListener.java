package com.mertaliakcay.malinesscore.confirmation;

import com.mertaliakcay.malinesscore.MaliNessCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class ConfirmationListener implements Listener {

    private final MaliNessCore plugin;

    public ConfirmationListener(MaliNessCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getConfirmationService().cancel(event.getPlayer().getUniqueId(), true);
        plugin.getTeleportService().cancelWarmup(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        plugin.getConfirmationService().cancel(event.getPlayer().getUniqueId(), true);
        plugin.getTeleportService().cancelWarmup(event.getPlayer().getUniqueId());
    }
}
