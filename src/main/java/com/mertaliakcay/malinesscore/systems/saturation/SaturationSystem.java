package com.mertaliakcay.malinesscore.systems.saturation;

import com.mertaliakcay.malinesscore.systems.AbstractGameSystem;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

import java.util.List;

public final class SaturationSystem extends AbstractGameSystem {

    public static final String PERM_USE = "maliness-core.saturation.use";
    public static final String ALIAS_TURKISH = "doygunluk";

    public static final int MAX_POINTS = 20;
    public static final int MIN_SET = 1;
    public static final int MAX_SET = 20;
    public static final int MIN_ADD = 1;
    public static final int MAX_ADD = 20;
    public static final int MIN_REMOVE = 1;
    public static final int MAX_REMOVE = 19;

    private SaturationCommand saturationCommand;

    @Override
    protected String getSystemId() {
        return "saturation";
    }

    @Override
    protected void onEnable() {
        if (!config.get().getBoolean("enabled", true)) {
            return;
        }

        saturationCommand = new SaturationCommand(this);
        SaturationBasicCommand saturationBasicCommand = new SaturationBasicCommand(saturationCommand);

        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            event.registrar().register(
                    "saturation",
                    "Oyuncu doygunluğunu ayarlar.",
                    List.of(ALIAS_TURKISH),
                    saturationBasicCommand
            );
        });

        plugin.getMalinessCommand().setSaturation(this, saturationCommand);
    }

    @Override
    protected void onDisable() {
        plugin.getMalinessCommand().clearSaturation();
        saturationCommand = null;
    }

    public boolean isEnabled() {
        return config.get().getBoolean("enabled", true);
    }
}
