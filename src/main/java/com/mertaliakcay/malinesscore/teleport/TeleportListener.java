package com.mertaliakcay.malinesscore.teleport;

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
import java.util.function.Predicate;

public final class TeleportListener implements Listener {

    private final TeleportService teleportService;
    private final Predicate<Player> vehicleBypassChecker;

    public TeleportListener(TeleportService teleportService, Predicate<Player> vehicleBypassChecker) {
        this.teleportService = teleportService;
        this.vehicleBypassChecker = vehicleBypassChecker;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        teleportService.cancelWarmup(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!teleportService.hasWarmup(event.getPlayer().getUniqueId())) {
            return;
        }

        teleportService.markMoved(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (teleportService.hasWarmup(player.getUniqueId())) {
            teleportService.markDamaged(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        if (teleportService.hasWarmup(player.getUniqueId())) {
            teleportService.markAttacked(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (!(event.getEntered() instanceof Player player)) {
            return;
        }

        if (teleportService.hasWarmup(player.getUniqueId())
                && !vehicleBypassChecker.test(player)) {
            teleportService.markVehicle(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGlide(EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (event.isGliding() && teleportService.hasWarmup(player.getUniqueId())) {
            teleportService.markGliding(player.getUniqueId());
        }
    }
}
