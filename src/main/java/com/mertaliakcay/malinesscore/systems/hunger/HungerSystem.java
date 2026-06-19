package com.mertaliakcay.malinesscore.systems.hunger;

import com.mertaliakcay.malinesscore.systems.AbstractGameSystem;

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
    protected void onRegister() {
        if (hungerCommand == null) {
            hungerCommand = new HungerCommand(this);
        }

        registerLifecycleCommandsOnce(registrar -> registrar.register(
                "hunger",
                "Oyuncu açlığını ayarlar.",
                List.of(ALIAS_TURKISH),
                new HungerBasicCommand(hungerCommand)
        ));

        plugin.getMalinessCommand().setHunger(this, hungerCommand);
    }

    @Override
    protected void onEnable() {
    }

    @Override
    protected void onDisable() {
    }

    @Override
    protected void onUnregister() {
        plugin.getMalinessCommand().clearHunger();
    }
}
