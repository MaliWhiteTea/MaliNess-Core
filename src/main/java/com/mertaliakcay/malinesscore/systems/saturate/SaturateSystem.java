package com.mertaliakcay.malinesscore.systems.saturate;

import com.mertaliakcay.malinesscore.systems.AbstractGameSystem;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

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
    protected void onEnable() {
        if (!config.get().getBoolean("enabled", true)) {
            return;
        }

        saturateCommand = new SaturateCommand(this);
        SaturateBasicCommand saturateBasicCommand = new SaturateBasicCommand(saturateCommand);

        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            event.registrar().register(
                    "saturate",
                    "Doygunluğu doldurur ve açlığı giderir.",
                    List.of(ALIAS_TURKISH),
                    saturateBasicCommand
            );
        });

        plugin.getMalinessCommand().setSaturate(this, saturateCommand);
    }

    @Override
    protected void onDisable() {
        plugin.getMalinessCommand().clearSaturate();
        saturateCommand = null;
    }

    public boolean isEnabled() {
        return config.get().getBoolean("enabled", true);
    }
}
