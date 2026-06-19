package com.mertaliakcay.malinesscore.systems.home;

import com.mertaliakcay.malinesscore.MaliNessCore;
import com.mertaliakcay.malinesscore.systems.home.model.HomeLocation;
import com.mertaliakcay.malinesscore.util.SystemLang;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class HomeTeleportManager {

    private static final double MOVE_THRESHOLD_SQUARED = 0.01D;

    private final MaliNessCore plugin;
    private final Map<UUID, PendingWarmup> warmups = new ConcurrentHashMap<>();

    private int warmupSeconds = 5;
    private int fireResistanceSeconds = 3;
    private SystemLang lang;

    public HomeTeleportManager(MaliNessCore plugin) {
        this.plugin = plugin;
    }

    public void configure(int warmupSeconds, int fireResistanceSeconds, SystemLang lang) {
        this.warmupSeconds = warmupSeconds;
        this.fireResistanceSeconds = fireResistanceSeconds;
        this.lang = lang;
    }

    public boolean hasWarmup(UUID playerId) {
        return warmups.containsKey(playerId);
    }

    public void cancelWarmup(UUID playerId) {
        PendingWarmup warmup = warmups.remove(playerId);
        if (warmup != null) {
            warmup.task.cancel();
        }
    }

    public void cancelAllWarmups() {
        for (UUID playerId : warmups.keySet().toArray(new UUID[0])) {
            cancelWarmup(playerId);
        }
    }

    public void startWarmup(Player player, HomeLocation home, Runnable onComplete) {
        cancelWarmup(player.getUniqueId());

        if (player.isInsideVehicle() && !HomeSystem.bypassesHomeRestrictions(player)) {
            lang.send(player, "teleport-vehicle-blocked");
            return;
        }

        Location startLocation = player.getLocation().clone();

        BukkitRunnable task = new BukkitRunnable() {
            int elapsedSeconds = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancelWarmup(player.getUniqueId());
                    return;
                }

                PendingWarmup warmup = warmups.get(player.getUniqueId());
                if (warmup == null || warmup.cancelled) {
                    cancel();
                    return;
                }

                if (checkInterrupt(player, warmup)) {
                    return;
                }

                elapsedSeconds++;
                int secondsLeft = warmupSeconds - elapsedSeconds;

                if (secondsLeft > 0 && secondsLeft < warmupSeconds) {
                    lang.send(player, "teleport-countdown", "seconds", secondsLeft);
                }

                if (elapsedSeconds >= warmupSeconds) {
                    lang.send(player, "teleport-starting");
                    executeTeleport(player, home, onComplete);
                    cancelWarmup(player.getUniqueId());
                    return;
                }
            }
        };

        PendingWarmup pending = new PendingWarmup(task, startLocation);
        warmups.put(player.getUniqueId(), pending);
        task.runTaskTimer(plugin, 0L, 20L);
    }

    public void teleportInstant(Player player, HomeLocation home, Runnable onComplete) {
        executeTeleport(player, home, onComplete);
    }

    public void markMoved(Player player) {
        PendingWarmup warmup = warmups.get(player.getUniqueId());
        if (warmup == null || warmup.moved) {
            return;
        }

        if (hasMoved(player.getLocation(), warmup.startLocation)) {
            warmup.moved = true;
            cancelWarmupWithMessage(player.getUniqueId());
        }
    }

    public void markDamaged(UUID playerId) {
        PendingWarmup warmup = warmups.get(playerId);
        if (warmup != null && !warmup.damaged) {
            warmup.damaged = true;
            cancelWarmupWithMessage(playerId);
        }
    }

    public void markAttacked(UUID playerId) {
        PendingWarmup warmup = warmups.get(playerId);
        if (warmup != null && !warmup.attacked) {
            warmup.attacked = true;
            cancelWarmupWithMessage(playerId);
        }
    }

    public void markVehicle(UUID playerId) {
        PendingWarmup warmup = warmups.get(playerId);
        if (warmup != null && !warmup.vehicle) {
            warmup.vehicle = true;
            cancelWarmupWithMessage(playerId);
        }
    }

    public void markGliding(UUID playerId) {
        PendingWarmup warmup = warmups.get(playerId);
        if (warmup != null && !warmup.gliding) {
            warmup.gliding = true;
            cancelWarmupWithMessage(playerId);
        }
    }

    private boolean checkInterrupt(Player player, PendingWarmup warmup) {
        if (warmup.moved || warmup.damaged || warmup.attacked || warmup.vehicle || warmup.gliding) {
            return true;
        }

        if (player.isInsideVehicle() && !HomeSystem.bypassesHomeRestrictions(player)) {
            warmup.vehicle = true;
            cancelWarmupWithMessage(player.getUniqueId());
            return true;
        }

        if (hasMoved(player.getLocation(), warmup.startLocation)) {
            warmup.moved = true;
            cancelWarmupWithMessage(player.getUniqueId());
            return true;
        }

        return false;
    }

    private boolean hasMoved(Location current, Location start) {
        if (current.getWorld() == null || start.getWorld() == null) {
            return true;
        }

        if (!current.getWorld().equals(start.getWorld())) {
            return true;
        }

        return current.distanceSquared(start) > MOVE_THRESHOLD_SQUARED;
    }

    private void cancelWarmupWithMessage(UUID playerId) {
        PendingWarmup warmup = warmups.remove(playerId);
        if (warmup == null) {
            return;
        }

        warmup.task.cancel();
        Player player = plugin.getServer().getPlayer(playerId);
        if (player != null && player.isOnline()) {
            lang.send(player, "teleport-cancelled");
        }
    }

    private void executeTeleport(Player player, HomeLocation home, Runnable onComplete) {
        Location location = home.toLocation();
        if (location == null || location.getWorld() == null) {
            lang.send(player, "world-not-loaded");
            return;
        }

        World world = location.getWorld();
        world.getChunkAtAsync(location).whenComplete((chunk, chunkError) -> plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }

            if (chunkError != null) {
                plugin.getLogger().log(Level.SEVERE, "Ev chunk yüklenemedi: " + player.getName(), chunkError);
                lang.send(player, "teleport-failed");
                return;
            }

            player.teleportAsync(location).whenComplete((success, teleportError) -> plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) {
                    return;
                }

                if (teleportError != null) {
                    plugin.getLogger().log(Level.SEVERE, "Ev ışınlanması başarısız: " + player.getName(), teleportError);
                    lang.send(player, "teleport-failed");
                    return;
                }

                if (Boolean.TRUE.equals(success)) {
                    player.addPotionEffect(new PotionEffect(
                            PotionEffectType.FIRE_RESISTANCE,
                            fireResistanceSeconds * 20,
                            0,
                            false,
                            false,
                            true
                    ));
                    if (onComplete != null) {
                        onComplete.run();
                    }
                } else {
                    lang.send(player, "teleport-failed");
                }
            }));
        }));
    }

    private static final class PendingWarmup {
        private final BukkitRunnable task;
        private final Location startLocation;
        private boolean cancelled;
        private boolean moved;
        private boolean damaged;
        private boolean attacked;
        private boolean vehicle;
        private boolean gliding;

        private PendingWarmup(BukkitRunnable task, Location startLocation) {
            this.task = task;
            this.startLocation = startLocation;
        }
    }
}
