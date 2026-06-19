package com.mertaliakcay.malinesscore.systems.heal;

import com.mertaliakcay.malinesscore.systems.AbstractGameSystem;

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
    protected void onRegister() {
        if (healCommand == null) {
            healCommand = new HealCommand(this);
        }

        registerLifecycleCommandsOnce(registrar -> registrar.register(
                "heal",
                "Can yeniler.",
                List.of(ALIAS_TURKISH),
                new HealBasicCommand(healCommand)
        ));

        plugin.getMalinessCommand().setHeal(this, healCommand);
    }

    @Override
    protected void onEnable() {
    }

    @Override
    protected void onDisable() {
    }

    @Override
    protected void onUnregister() {
        plugin.getMalinessCommand().clearHeal();
    }
}
