package com.mertaliakcay.malinesscore.systems.saturate;

import com.mertaliakcay.malinesscore.systems.AbstractGameSystem;

import java.util.List;

public final class SaturateSystem extends AbstractGameSystem {

    public static final String PERM_USE = "maliness-core.saturate.use";
    public static final String PERM_OTHERS = "maliness-core.saturate.use.others";
    public static final String ALIAS_TURKISH = "tokla";

    private SaturateCommand saturateCommand;

    @Override
    protected String getSystemId() {
        return "saturate";
    }

    @Override
    protected void onRegister() {
        if (saturateCommand == null) {
            saturateCommand = new SaturateCommand(this);
        }

        registerLifecycleCommandsOnce(registrar -> registrar.register(
                "saturate",
                "Doygunluğu doldurur ve açlığı giderir.",
                List.of(ALIAS_TURKISH),
                new SaturateBasicCommand(saturateCommand)
        ));

        plugin.getMalinessCommand().setSaturate(this, saturateCommand);
    }

    @Override
    protected void onEnable() {
    }

    @Override
    protected void onDisable() {
    }

    @Override
    protected void onUnregister() {
        plugin.getMalinessCommand().clearSaturate();
    }
}
