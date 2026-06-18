package com.mertaliakcay.malinesscore.systems.heal;

import com.mertaliakcay.malinesscore.command.MalinessCommand;
import com.mertaliakcay.malinesscore.systems.AbstractGameSystem;
import com.mertaliakcay.malinesscore.util.CommandRegistrar;
import org.bukkit.command.PluginCommand;

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

        PluginCommand healPluginCommand = plugin.getCommand("heal");
        if (healPluginCommand == null) {
            plugin.getLogger().severe("heal komutu plugin.yml içinde tanımlı değil!");
            return;
        }

        healCommand = new HealCommand(this);
        healPluginCommand.setExecutor(healCommand);
        healPluginCommand.setTabCompleter(healCommand);

        PluginCommand malinessPluginCommand = plugin.getCommand("maliness");
        if (malinessPluginCommand != null) {
            malinessCommand = new MalinessCommand(this);
            malinessCommand.setHealCommand(healCommand);
            malinessPluginCommand.setExecutor(malinessCommand);
            malinessPluginCommand.setTabCompleter(malinessCommand);
        }

        if (config.get().getBoolean("override-command", true)) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                CommandRegistrar.override(plugin, "heal", healPluginCommand);
                plugin.getPluginLang().logInfo("command-overridden", "command", "heal");
            }, 1L);
        }
    }

    @Override
    protected void onDisable() {
        PluginCommand healPluginCommand = plugin.getCommand("heal");
        if (healPluginCommand != null) {
            healPluginCommand.setExecutor(null);
            healPluginCommand.setTabCompleter(null);
        }

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
