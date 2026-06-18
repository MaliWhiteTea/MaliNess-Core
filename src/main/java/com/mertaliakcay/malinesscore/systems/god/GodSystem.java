package com.mertaliakcay.malinesscore.systems.god;

import com.mertaliakcay.malinesscore.systems.AbstractGameSystem;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
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
    private GodListener godListener;

    @Override
    protected String getSystemId() {
        return "god";
    }

    @Override
    protected void onEnable() {
        if (!config.get().getBoolean("enabled", true)) {
            return;
        }

        godCommand = new GodCommand(this);
        GodBasicCommand godBasicCommand = new GodBasicCommand(godCommand);
        godListener = new GodListener(this);

        plugin.getServer().getPluginManager().registerEvents(godListener, plugin);

        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            event.registrar().register(
                    "god",
                    "God modunu açar veya kapatır.",
                    List.of(ALIAS_TURKISH),
                    godBasicCommand
            );
        });

        plugin.getMalinessCommand().setGod(this, godCommand);
    }

    @Override
    protected void onDisable() {
        plugin.getMalinessCommand().clearGod();
        godPlayers.clear();
        godListener = null;
        godCommand = null;
    }

    public boolean isEnabled() {
        return config.get().getBoolean("enabled", true);
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
}
