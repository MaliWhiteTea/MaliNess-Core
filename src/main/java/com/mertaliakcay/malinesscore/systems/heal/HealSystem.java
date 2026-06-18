package com.mertaliakcay.malinesscore.systems.heal;

import com.mertaliakcay.malinesscore.command.MalinessCommand;
import com.mertaliakcay.malinesscore.systems.AbstractGameSystem;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.command.PluginCommand;

import java.util.Collections;

public final class HealSystem extends AbstractGameSystem {

    public static final String PERM_USE = "maliness-core.heal.use";
    public static final String PERM_OTHERS = "maliness-core.heal.use.others";

    private HealCommand healCommand;
    private MalinessCommand malinessCommand;

    @Override
    protected String getSystemId() {
        return "heal";
    }

    @Override
    protected void onEnable() {
        if (!config.get().getBoolean("enabled", true)) {
            return;
        }

        healCommand = new HealCommand(this);
        HealBasicCommand healBasicCommand = new HealBasicCommand(healCommand);

        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            event.registrar().register(
                    "heal",
                    "Can yeniler.",
                    Collections.emptyList(),
                    healBasicCommand
            );
        });

        PluginCommand malinessPluginCommand = plugin.getCommand("maliness");
        if (malinessPluginCommand != null) {
            malinessCommand = new MalinessCommand(this);
            malinessCommand.setHealCommand(healCommand);
            malinessPluginCommand.setExecutor(malinessCommand);
            malinessPluginCommand.setTabCompleter(malinessCommand);
        }
    }

    @Override
    protected void onDisable() {
        PluginCommand malinessPluginCommand = plugin.getCommand("maliness");
        if (malinessPluginCommand != null) {
            malinessPluginCommand.setExecutor(null);
            malinessPluginCommand.setTabCompleter(null);
        }

        healCommand = null;
        malinessCommand = null;
    }

    public boolean isEnabled() {
        return config.get().getBoolean("enabled", true);
    }
}
