package com.mertaliakcay.malinesscore.systems.heal;

import com.mertaliakcay.malinesscore.systems.AbstractGameSystem;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

import java.util.List;

public final class HealSystem extends AbstractGameSystem {

    public static final String PERM_USE = "maliness-core.heal.use";
    public static final String PERM_OTHERS = "maliness-core.heal.use.others";
    public static final String ALIAS_TURKISH = "iyileştir";

    private HealCommand healCommand;

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
                    List.of(ALIAS_TURKISH),
                    healBasicCommand
            );
        });

        plugin.getMalinessCommand().setHeal(this, healCommand);
    }

    @Override
    protected void onDisable() {
        plugin.getMalinessCommand().clearHeal();
        healCommand = null;
    }

    public boolean isEnabled() {
        return config.get().getBoolean("enabled", true);
    }
}
