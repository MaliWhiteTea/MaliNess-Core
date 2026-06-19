package com.mertaliakcay.malinesscore.systems.saturation;

import com.mertaliakcay.malinesscore.systems.AbstractGameSystem;

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
    protected void onRegister() {
        if (saturationCommand == null) {
            saturationCommand = new SaturationCommand(this);
        }

        registerLifecycleCommandsOnce(registrar -> registrar.register(
                "saturation",
                "Oyuncu doygunluğunu ayarlar.",
                List.of(ALIAS_TURKISH),
                new SaturationBasicCommand(saturationCommand)
        ));

        plugin.getMalinessCommand().setSaturation(this, saturationCommand);
    }

    @Override
    protected void onEnable() {
    }

    @Override
    protected void onDisable() {
    }

    @Override
    protected void onUnregister() {
        plugin.getMalinessCommand().clearSaturation();
    }
}
