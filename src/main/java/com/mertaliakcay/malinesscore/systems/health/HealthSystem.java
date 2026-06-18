package com.mertaliakcay.malinesscore.systems.health;

import com.mertaliakcay.malinesscore.systems.AbstractGameSystem;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

import java.util.List;

public final class HealthSystem extends AbstractGameSystem {

    public static final String PERM_USE = "maliness-core.health.use";
    public static final String ALIAS_TURKISH = "sağlık";

    public static final int MAX_POINTS = 20;
    public static final int MIN_SET = 1;
    public static final int MAX_SET = 20;
    public static final int MIN_ADD = 1;
    public static final int MAX_ADD = 20;
    public static final int MIN_REMOVE = 1;
    public static final int MAX_REMOVE = 19;

    private HealthCommand healthCommand;

    @Override
    protected String getSystemId() {
        return "health";
    }

    @Override
    protected void onEnable() {
        if (!config.get().getBoolean("enabled", true)) {
            return;
        }

        healthCommand = new HealthCommand(this);
        HealthBasicCommand healthBasicCommand = new HealthBasicCommand(healthCommand);

        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            event.registrar().register(
                    "health",
                    "Oyuncu canını ayarlar.",
                    List.of(ALIAS_TURKISH),
                    healthBasicCommand
            );
        });

        plugin.getMalinessCommand().setHealth(this, healthCommand);
    }

    @Override
    protected void onDisable() {
        plugin.getMalinessCommand().clearHealth();
        healthCommand = null;
    }

    public boolean isEnabled() {
        return config.get().getBoolean("enabled", true);
    }
}
