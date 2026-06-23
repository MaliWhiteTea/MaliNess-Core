package com.mertaliakcay.malinesscore.gui.model;

public final class MenuClickProtectionSettings {

    private final boolean doubleClickGuard;
    private final boolean buttonCooldownEnabled;
    private final long buttonCooldownMillis;

    public MenuClickProtectionSettings(boolean doubleClickGuard, boolean buttonCooldownEnabled, long buttonCooldownMillis) {
        this.doubleClickGuard = doubleClickGuard;
        this.buttonCooldownEnabled = buttonCooldownEnabled;
        this.buttonCooldownMillis = buttonCooldownMillis;
    }

    public boolean isDoubleClickGuard() {
        return doubleClickGuard;
    }

    public boolean isButtonCooldownEnabled() {
        return buttonCooldownEnabled;
    }

    public long getButtonCooldownMillis() {
        return buttonCooldownMillis;
    }
}
