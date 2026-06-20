package com.mertaliakcay.malinesscore.systems.vanish;

import com.mertaliakcay.malinesscore.MaliNessCore;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class VanishService {

    private final MaliNessCore plugin;
    private final VanishStorage storage;
    private VanishPacketEnhancer packetEnhancer;

    public VanishService(MaliNessCore plugin, VanishStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    public void load() {
        storage.load();
    }

    public void save() {
        storage.save();
    }

    public void setPacketEnhancer(VanishPacketEnhancer packetEnhancer) {
        this.packetEnhancer = packetEnhancer;
    }

    public boolean isVanished(UUID playerId) {
        return storage.isVanished(playerId);
    }

    public boolean isVanished(Player player) {
        return player != null && isVanished(player.getUniqueId());
    }

    public boolean canSee(CommandSender viewer, Player target) {
        if (target == null || !isVanished(target)) {
            return true;
        }

        if (!(viewer instanceof Player player)) {
            return true;
        }

        return player.hasPermission(VanishSystem.PERM_SEE) || player.equals(target);
    }

    public void enableVanish(Player player) {
        storage.setVanished(player.getUniqueId(), true);
        applyVanish(player);
        save();
    }

    public void disableVanish(Player player) {
        storage.setVanished(player.getUniqueId(), false);
        removeVanish(player);
        save();
    }

    public void toggleVanish(Player player) {
        if (isVanished(player)) {
            disableVanish(player);
        } else {
            enableVanish(player);
        }
    }

    public void applyVanish(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!canSee(online, player)) {
                online.hidePlayer(plugin, player);
            }
        }

        clearMobTargets(player);
        if (packetEnhancer != null) {
            packetEnhancer.registerPlayer(player);
        }
    }

    public void removeVanish(Player player) {
        if (player == null) {
            return;
        }

        for (Player online : Bukkit.getOnlinePlayers()) {
            online.showPlayer(plugin, player);
        }

        if (packetEnhancer != null) {
            packetEnhancer.unregisterPlayer(player);
        }
    }

    public void handleJoin(Player player) {
        for (UUID vanishedId : storage.getVanishedIds()) {
            Player vanished = Bukkit.getPlayer(vanishedId);
            if (vanished != null && vanished.isOnline() && !canSee(player, vanished)) {
                player.hidePlayer(plugin, vanished);
            }
        }

        if (!isVanished(player)) {
            return;
        }

        applyVanish(player);
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (player.isOnline() && isVanished(player)) {
                VanishSystem vanishSystem = getVanishSystem();
                if (vanishSystem != null) {
                    vanishSystem.getLang().send(player, "reconnect-reminder");
                }
            }
        });
    }

    public void handleQuit(Player player) {
        if (packetEnhancer != null) {
            packetEnhancer.unregisterPlayer(player);
        }
    }

    public void refreshVisibility(Player viewer) {
        if (!(viewer instanceof Player)) {
            return;
        }

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (isVanished(online) && !canSee(viewer, online)) {
                viewer.hidePlayer(plugin, online);
            } else {
                viewer.showPlayer(plugin, online);
            }
        }
    }

    public List<String> filterPlayerNames(CommandSender viewer, List<String> names) {
        if (names == null || names.isEmpty()) {
            return List.of();
        }

        if (!(viewer instanceof Player player)) {
            return names;
        }

        return names.stream()
                .filter(name -> {
                    Player target = Bukkit.getPlayerExact(name);
                    return target == null || canSee(player, target);
                })
                .collect(Collectors.toList());
    }

    public List<Player> getOnlineVanishedPlayers() {
        List<Player> vanishedPlayers = new ArrayList<>();
        for (UUID playerId : storage.getVanishedIds()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                vanishedPlayers.add(player);
            }
        }
        return vanishedPlayers;
    }

    public Set<UUID> getVanishedIds() {
        return storage.getVanishedIds();
    }

    public int countOnlineVanished() {
        int count = 0;
        for (UUID playerId : storage.getVanishedIds()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                count++;
            }
        }
        return count;
    }

    public int countVisibleOnline(CommandSender viewer) {
        if (viewer instanceof Player player) {
            int count = 0;
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (canSee(player, online)) {
                    count++;
                }
            }
            return count;
        }

        return Bukkit.getOnlinePlayers().size() - countOnlineVanished();
    }

    public String formatVisibleOnlineNames(CommandSender viewer) {
        return Bukkit.getOnlinePlayers().stream()
                .filter(online -> canSee(viewer, online))
                .map(Player::getName)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.joining(", "));
    }

    public void clearMobTargets(Player player) {
        double radius = 64.0D;
        player.getNearbyEntities(radius, radius, radius).stream()
                .filter(Mob.class::isInstance)
                .map(Mob.class::cast)
                .filter(mob -> player.equals(mob.getTarget()))
                .forEach(mob -> mob.setTarget(null));
    }

    private VanishSystem getVanishSystem() {
        if (plugin.getSystemManager().findAbstractSystem("vanish") instanceof VanishSystem vanishSystem) {
            return vanishSystem;
        }
        return null;
    }
}
