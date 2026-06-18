package com.mertaliakcay.malinesscore.systems.hunger;

import com.mertaliakcay.malinesscore.systems.AbstractGameSystem;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

import java.util.List;

public final class HungerSystem extends AbstractGameSystem {

    public static final String PERM_USE = "maliness-core.hunger.use";
    public static final String ALIAS_TURKISH = "açlık";

    public static final int MAX_POINTS = 20;
    public static final int MIN_SET = 1;
    public static final int MAX_SET = 20;
    public static final int MIN_ADD = 1;
    public static final int MAX_ADD = 20;
    public static final int MIN_REMOVE = 1;
    public static final int MAX_REMOVE = 19;

    private HungerCommand hungerCommand;

    @Override
    protected String getSystemId() {
        return "hunger";
    }

    @Override
    protected void onEnable() {
        if (!config.get().getBoolean("enabled", true)) {
            return;
        }

        hungerCommand = new HungerCommand(this);
        HungerBasicCommand hungerBasicCommand = new HungerBasicCommand(hungerCommand);

        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            event.registrar().register(
                    "hunger",
                    "Oyuncu açlığını ayarlar.",
                    List.of(ALIAS_TURKISH),
                    hungerBasicCommand
            );
        });

        plugin.getMalinessCommand().setHunger(this, hungerCommand);
    }

    @Override
    protected void onDisable() {
        plugin.getMalinessCommand().clearHunger();
        hungerCommand = null;
    }

    public boolean isEnabled() {
        return config.get().getBoolean("enabled", true);
    }
}
