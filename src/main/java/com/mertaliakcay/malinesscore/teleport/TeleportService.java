package com.mertaliakcay.malinesscore.teleport;

import com.mertaliakcay.malinesscore.MaliNessCore;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.logging.Level;

public final class TeleportService {

    private static final double MOVE_THRESHOLD_SQUARED = 0.01D;

    private final MaliNessCore plugin;
    private final Map<UUID, PendingWarmup> warmups = new ConcurrentHashMap<>();

    public TeleportService(MaliNessCore plugin) {
        this.plugin = plugin;
    }

    public boolean hasWarmup(UUID playerId) {
        return warmups.containsKey(playerId);
    }

    public WarmupType getWarmupType(UUID playerId) {
        PendingWarmup warmup = warmups.get(playerId);
        return warmup == null ? null : warmup.type;
    }

    public int getWarmupRemainingSeconds(UUID playerId) {
        PendingWarmup warmup = warmups.get(playerId);
        if (warmup == null) {
            return 0;
        }

        long elapsedSeconds = (System.currentTimeMillis() - warmup.startedAtMillis) / 1000L;
        return Math.max(0, warmup.warmupSeconds - (int) elapsedSeconds);
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

    public void startWarmup(
            Player player,
            TeleportDestination destination,
            TeleportMessages messages,
            int warmupSeconds,
            int fireResistanceSeconds,
            Predicate<Player> bypassRestrictions,
            WarmupType type,
            Runnable onComplete
    ) {
        cancelWarmup(player.getUniqueId());

        if (player.isInsideVehicle() && !bypassRestrictions.test(player)) {
            messages.sendVehicleBlocked(player);
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
                int secondsLeft = warmup.warmupSeconds - elapsedSeconds;

                if (secondsLeft > 0 && secondsLeft < warmup.warmupSeconds) {
                    warmup.messages.sendCountdown(player, secondsLeft);
                }

                if (elapsedSeconds >= warmup.warmupSeconds) {
                    warmup.messages.sendStarting(player);
                    executeTeleport(player, warmup.destination, warmup.fireResistanceSeconds, warmup.messages, onComplete);
                    cancelWarmup(player.getUniqueId());
                }
            }
        };

        PendingWarmup pending = new PendingWarmup(
                task,
                startLocation,
                destination,
                messages,
                warmupSeconds,
                fireResistanceSeconds,
                bypassRestrictions,
                type
        );
        warmups.put(player.getUniqueId(), pending);
        task.runTaskTimer(plugin, 0L, 20L);
    }

    public void teleportInstant(
            Player player,
            TeleportDestination destination,
            TeleportMessages messages,
            int fireResistanceSeconds,
            Runnable onComplete
    ) {
        executeTeleport(player, destination, fireResistanceSeconds, messages, onComplete);
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

        if (player.isInsideVehicle() && !warmup.bypassRestrictions.test(player)) {
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
            warmup.messages.sendCancelled(player);
        }
    }

    private void executeTeleport(
            Player player,
            TeleportDestination destination,
            int fireResistanceSeconds,
            TeleportMessages messages,
            Runnable onComplete
    ) {
        Location location = destination.toLocation();
        if (location == null || location.getWorld() == null) {
            messages.sendWorldNotLoaded(player);
            return;
        }

        World world = location.getWorld();
        world.getChunkAtAsync(location).whenComplete((chunk, chunkError) -> plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }

            if (chunkError != null) {
                plugin.getLogger().log(Level.SEVERE, "Işınlanma chunk yüklenemedi: " + player.getName(), chunkError);
                messages.sendFailed(player);
                return;
            }

            player.teleportAsync(location).whenComplete((success, teleportError) -> plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) {
                    return;
                }

                if (teleportError != null) {
                    plugin.getLogger().log(Level.SEVERE, "Işınlanma başarısız: " + player.getName(), teleportError);
                    messages.sendFailed(player);
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
                    messages.sendFailed(player);
                }
            }));
        }));
    }

    private static final class PendingWarmup {
        private final BukkitRunnable task;
        private final Location startLocation;
        private final TeleportDestination destination;
        private final TeleportMessages messages;
        private final int warmupSeconds;
        private final int fireResistanceSeconds;
        private final Predicate<Player> bypassRestrictions;
        private final WarmupType type;
        private final long startedAtMillis;
        private boolean cancelled;
        private boolean moved;
        private boolean damaged;
        private boolean attacked;
        private boolean vehicle;
        private boolean gliding;

        private PendingWarmup(
                BukkitRunnable task,
                Location startLocation,
                TeleportDestination destination,
                TeleportMessages messages,
                int warmupSeconds,
                int fireResistanceSeconds,
                Predicate<Player> bypassRestrictions,
                WarmupType type
        ) {
            this.task = task;
            this.startLocation = startLocation;
            this.destination = destination;
            this.messages = messages;
            this.warmupSeconds = warmupSeconds;
            this.fireResistanceSeconds = fireResistanceSeconds;
            this.bypassRestrictions = bypassRestrictions;
            this.type = type;
            this.startedAtMillis = System.currentTimeMillis();
        }
    }
}
