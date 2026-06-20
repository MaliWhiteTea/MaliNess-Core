package com.mertaliakcay.malinesscore.systems.broadcast;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class BroadcastSystem extends com.mertaliakcay.malinesscore.systems.AbstractGameSystem {

    public static final String PERM_USE = "maliness-core.broadcast.use";
    public static final String ALIAS_BC = "bc";
    public static final String ALIAS_DUYUR = "duyur";
    public static final String ALIAS_DUYURUYAP = "duyuruyap";

    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    private BroadcastCommand broadcastCommand;
    private BroadcastMnCommand broadcastMnCommand;

    @Override
    protected String getSystemId() {
        return "broadcast";
    }

    @Override
    protected void onRegister() {
        if (broadcastCommand == null) {
            broadcastCommand = new BroadcastCommand(this);
        }

        registerLifecycleCommandsOnce(registrar -> registrar.register(
                "broadcast",
                "Sunucu duyurusu gönderir.",
                List.of(ALIAS_BC, ALIAS_DUYUR, ALIAS_DUYURUYAP),
                new BroadcastBasicCommand(broadcastCommand)
        ));

        broadcastMnCommand = new BroadcastMnCommand(broadcastCommand);
        plugin.getMalinessCommand().setBroadcast(this, broadcastMnCommand);
    }

    @Override
    protected void onEnable() {
        broadcastCommand.reloadFromConfig();
    }

    @Override
    protected void onDisable() {
        cooldowns.clear();
    }

    @Override
    protected void onUnregister() {
        plugin.getMalinessCommand().clearBroadcast();
    }

    Map<UUID, Long> getCooldowns() {
        return cooldowns;
    }
}
