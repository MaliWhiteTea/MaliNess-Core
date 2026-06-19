package com.mertaliakcay.malinesscore.systems.god;

import com.mertaliakcay.malinesscore.systems.AbstractGameSystem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class GodSystem extends AbstractGameSystem {

    public static final String PERM_USE = "maliness-core.god.use";
    public static final String PERM_OTHERS = "maliness-core.god.use.others";
    public static final String ALIAS_TURKISH = "tanrı";

    private final Set<UUID> godPlayers = new HashSet<>();

    private GodCommand godCommand;
    private GodStateStorage stateStorage;

    @Override
    protected String getSystemId() {
        return "god";
    }

    @Override
    protected void onRegister() {
        if (godCommand == null) {
            godCommand = new GodCommand(this);
        }

        registerLifecycleCommandsOnce(registrar -> registrar.register(
                "god",
                "God modunu açar veya kapatır.",
                List.of(ALIAS_TURKISH),
                new GodBasicCommand(godCommand)
        ));

        plugin.getMalinessCommand().setGod(this, godCommand);
    }

    @Override
    protected void onEnable() {
        if (stateStorage == null) {
            stateStorage = new GodStateStorage(plugin);
        }

        registerListener(new GodListener(this));

        if (plugin.isReloading()) {
            restoreGodStateAfterReload();
        }
    }

    @Override
    protected void onDisable() {
        if (stateStorage == null) {
            stateStorage = new GodStateStorage(plugin);
        }

        if (plugin.isReloading()) {
            stateStorage.save(godPlayers);
        } else {
            stateStorage.delete();
        }

        godPlayers.clear();
    }

    @Override
    protected void onUnregister() {
        plugin.getMalinessCommand().clearGod();
    }

    public boolean isGod(Player player) {
        return godPlayers.contains(player.getUniqueId());
    }

    public boolean toggleGod(Player player) {
        if (isGod(player)) {
            disableGod(player);
            return false;
        }

        enableGod(player);
        return true;
    }

    public void setGod(Player player, boolean enabled) {
        if (enabled) {
            enableGod(player);
        } else {
            disableGod(player);
        }
    }

    public void enableGod(Player player) {
        godPlayers.add(player.getUniqueId());
        clearHostileTargets(player);
    }

    public void disableGod(Player player) {
        godPlayers.remove(player.getUniqueId());
    }

    public void handleQuit(Player player) {
        godPlayers.remove(player.getUniqueId());
    }

    private void clearHostileTargets(Player player) {
        double radius = config.get().getDouble("clear-target-radius", 64.0D);

        player.getNearbyEntities(radius, radius, radius).stream()
                .filter(Mob.class::isInstance)
                .map(Mob.class::cast)
                .filter(mob -> player.equals(mob.getTarget()))
                .forEach(mob -> mob.setTarget(null));
    }

    private void restoreGodStateAfterReload() {
        godPlayers.addAll(stateStorage.load());
        stateStorage.delete();

        for (UUID playerId : godPlayers) {
            Player online = Bukkit.getPlayer(playerId);
            if (online != null && online.isOnline()) {
                clearHostileTargets(online);
            }
        }
    }
}
