package com.mertaliakcay.malinesscore.systems.warp;

import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class WarpSystem extends com.mertaliakcay.malinesscore.systems.AbstractGameSystem {

    public static final String PERM_USE = "maliness-core.warp.use";
    public static final String PERM_MANAGE = "maliness-core.warp.manage";
    public static final String PERM_SEE_CLOSED = "maliness-core.warp.see-closed";
    public static final String PERM_BYPASS_TIME = "maliness-core.warp.bypasstime";
    public static final String PERM_INVALID_WARP_BROADCAST = "maliness-core.warp.invalid.broadcast";

    public static final String ALIAS_WARPLAR = "warplar";

    public static final List<String> SET_ALIASES = List.of("ekle", "set");
    public static final List<String> REMOVE_ALIASES = List.of("sil", "remove");
    public static final List<String> EDIT_ALIASES = List.of("düzenle", "edit");

    public static final Set<String> EDIT_ACTIONS = Set.of("konum", "açıklama", "aciklama");
    public static final Set<String> LOCATION_ACTIONS = Set.of("konum");
    public static final Set<String> DESCRIPTION_ACTIONS = Set.of("açıklama", "aciklama");
    public static final Set<String> ENABLE_ACTIONS = Set.of("açık", "acik", "on", "aktif", "active");
    public static final Set<String> DISABLE_ACTIONS = Set.of("kapalı", "kapali", "off", "deaktif", "deactive");

    public static boolean bypassesWarpRestrictions(Player player) {
        return player.isOp() || player.hasPermission(PERM_BYPASS_TIME);
    }

    private WarpStorage storage;
    private WarpService warpService;
    private WarpCommand warpCommand;
    private WarpMnCommand warpMnCommand;

    @Override
    protected String getSystemId() {
        return "warp";
    }

    @Override
    protected void onRegister() {
        if (warpCommand == null) {
            warpCommand = new WarpCommand(this);
        }

        registerLifecycleCommandsOnce(registrar -> {
            registrar.register(
                    "warp",
                    "Warp noktalarına ışınlanır veya yönetir.",
                    List.of(),
                    new WarpBasicCommand(warpCommand)
            );
            registrar.register(
                    "warps",
                    "Warp noktalarını listeler.",
                    List.of(WarpSystem.ALIAS_WARPLAR),
                    new WarpsBasicCommand(warpCommand)
            );
        });
    }

    @Override
    protected void onEnable() {
        WarpNameValidator nameValidator = new WarpNameValidator(
                config.get().getStringList("names.reserved")
        );

        if (storage == null) {
            storage = new WarpStorage(plugin, lang);
        }

        storage.load();
        storage.validateWorlds();

        WarpLogger warpLogger = new WarpLogger(
                plugin,
                config.get().getBoolean("logging.player-actions", true),
                config.get().getBoolean("logging.admin-actions", true)
        );

        if (warpService == null) {
            warpService = new WarpService(
                    plugin,
                    this,
                    storage,
                    nameValidator,
                    warpLogger,
                    plugin.getTeleportService(),
                    plugin.getConfirmationService()
            );
        } else {
            warpService = new WarpService(
                    plugin,
                    this,
                    storage,
                    nameValidator,
                    warpLogger,
                    plugin.getTeleportService(),
                    plugin.getConfirmationService()
            );
        }

        warpService.reloadFromConfig();
        warpMnCommand = new WarpMnCommand(warpCommand);
        plugin.getMalinessCommand().setWarp(this, warpMnCommand);
    }

    @Override
    protected void onDeactivate() {
        plugin.getTeleportService().cancelAllWarmups();
    }

    @Override
    protected void onDisable() {
        onDeactivate();
        if (storage != null) {
            storage.save();
        }
    }

    @Override
    protected void onUnregister() {
        plugin.getMalinessCommand().clearWarp();
    }

    public WarpService getWarpService() {
        return warpService;
    }

    public WarpStorage getStorage() {
        return storage;
    }
}
