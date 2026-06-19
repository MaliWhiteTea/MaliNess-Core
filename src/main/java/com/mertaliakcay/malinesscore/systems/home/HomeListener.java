package com.mertaliakcay.malinesscore.systems.home;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;

import java.util.UUID;

public final class HomeListener implements Listener {

    private final HomeTeleportManager teleportManager;
    private final HomeService homeService;

    public HomeListener(HomeTeleportManager teleportManager, HomeService homeService) {
        this.teleportManager = teleportManager;
        this.homeService = homeService;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        teleportManager.cancelWarmup(playerId);
        homeService.cleanupPlayer(playerId);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!teleportManager.hasWarmup(event.getPlayer().getUniqueId())) {
            return;
        }

        teleportManager.markMoved(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (teleportManager.hasWarmup(player.getUniqueId())) {
            teleportManager.markDamaged(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        if (teleportManager.hasWarmup(player.getUniqueId())) {
            teleportManager.markAttacked(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (!(event.getEntered() instanceof Player player)) {
            return;
        }

        if (teleportManager.hasWarmup(player.getUniqueId())
                && !HomeSystem.bypassesHomeRestrictions(player)) {
            teleportManager.markVehicle(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGlide(EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (event.isGliding() && teleportManager.hasWarmup(player.getUniqueId())) {
            teleportManager.markGliding(player.getUniqueId());
        }
    }
}
