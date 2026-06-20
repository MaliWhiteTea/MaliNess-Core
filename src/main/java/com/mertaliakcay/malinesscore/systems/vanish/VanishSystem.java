package com.mertaliakcay.malinesscore.systems.vanish;

import com.mertaliakcay.malinesscore.MaliNessCore;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

public final class VanishSystem extends com.mertaliakcay.malinesscore.systems.AbstractGameSystem {

    public static final String PERM_USE = "maliness-core.vanish.use";
    public static final String PERM_OTHERS = "maliness-core.vanish.use.others";
    public static final String PERM_SEE = "maliness-core.vanish.see";
    public static final String ALIAS_TURKISH = "gizlen";

    private VanishStorage storage;
    private VanishService vanishService;
    private VanishCommand vanishCommand;
    private VanishMnCommand vanishMnCommand;
    private VanishPacketEnhancer packetEnhancer = new NoopVanishPacketEnhancer();

    private boolean joinQuitHidden = true;
    private boolean blockPrivateMessages = true;
    private boolean preventDamage = true;
    private boolean preventDealingDamage = true;
    private boolean preventItemPickup = true;
    private boolean preventMobTargeting = true;
    private boolean hideDeathMessages = true;
    private boolean hideAdvancementMessages = true;
    private boolean hideSculkSensor = true;
    private boolean hidePressurePlates = true;

    @Override
    protected String getSystemId() {
        return "vanish";
    }

    @Override
    protected void onRegister() {
        if (vanishCommand == null) {
            vanishCommand = new VanishCommand(this);
        }

        registerLifecycleCommandsOnce(registrar -> registrar.register(
                "vanish",
                "Gizli modu açar veya kapatır.",
                List.of(ALIAS_TURKISH),
                new VanishBasicCommand(vanishCommand)
        ));

        vanishMnCommand = new VanishMnCommand(vanishCommand);
        plugin.getMalinessCommand().setVanish(this, vanishMnCommand);
    }

    @Override
    protected void onEnable() {
        reloadSettings();

        if (storage == null) {
            storage = new VanishStorage(plugin);
        }

        if (vanishService == null) {
            vanishService = new VanishService(plugin, storage);
        }

        vanishService.load();
        vanishService.setPacketEnhancer(packetEnhancer);
        plugin.setVanishService(vanishService);

        setupPacketEnhancer();
    }

    @Override
    protected void onActivate() {
        registerListener(new VanishListener(this, vanishService, packetEnhancer));

        packetEnhancer.enable();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (vanishService.isVanished(player)) {
                vanishService.applyVanish(player);
            }
        }
    }

    @Override
    protected void onDeactivate() {
        if (packetEnhancer != null) {
            packetEnhancer.disable();
        }
    }

    @Override
    protected void onDisable() {
        onDeactivate();
        if (vanishService != null) {
            vanishService.save();
        }
        plugin.setVanishService(null);
    }

    @Override
    protected void onUnregister() {
        plugin.getMalinessCommand().clearVanish();
    }

    public VanishService getVanishService() {
        return vanishService;
    }

    public boolean isJoinQuitHidden() {
        return joinQuitHidden;
    }

    public boolean isBlockPrivateMessages() {
        return blockPrivateMessages;
    }

    public boolean isPreventDamage() {
        return preventDamage;
    }

    public boolean isPreventDealingDamage() {
        return preventDealingDamage;
    }

    public boolean isPreventItemPickup() {
        return preventItemPickup;
    }

    public boolean isPreventMobTargeting() {
        return preventMobTargeting;
    }

    public boolean isHideDeathMessages() {
        return hideDeathMessages;
    }

    public boolean isHideAdvancementMessages() {
        return hideAdvancementMessages;
    }

    public boolean isHideSculkSensor() {
        return hideSculkSensor;
    }

    public boolean isHidePressurePlates() {
        return hidePressurePlates;
    }

    private void reloadSettings() {
        FileConfiguration configuration = config.get();
        joinQuitHidden = configuration.getBoolean("join-quit-messages-hidden", true);
        blockPrivateMessages = configuration.getBoolean("block-private-messages", true);
        preventDamage = configuration.getBoolean("prevent-damage", true);
        preventDealingDamage = configuration.getBoolean("prevent-dealing-damage", true);
        preventItemPickup = configuration.getBoolean("prevent-item-pickup", true);
        preventMobTargeting = configuration.getBoolean("prevent-mob-targeting", true);
        hideDeathMessages = configuration.getBoolean("hide-death-messages", true);
        hideAdvancementMessages = configuration.getBoolean("hide-advancement-messages", true);
        hideSculkSensor = configuration.getBoolean("hide-sculk-sensor", true);
        hidePressurePlates = configuration.getBoolean("hide-pressure-plates", true);
    }

    private void setupPacketEnhancer() {
        if (packetEnhancer != null) {
            packetEnhancer.disable();
        }

        FileConfiguration configuration = config.get();
        boolean protocolLibEnabled = configuration.getBoolean("protocol-lib.enabled", true);
        Plugin protocolLib = Bukkit.getPluginManager().getPlugin("ProtocolLib");

        if (protocolLibEnabled && protocolLib != null && protocolLib.isEnabled()) {
            packetEnhancer = new ProtocolLibVanishEnhancer(
                    plugin,
                    vanishService,
                    configuration.getBoolean("protocol-lib.hide-chest-animation", true),
                    configuration.getBoolean("protocol-lib.hide-chest-sound", true),
                    configuration.getBoolean("protocol-lib.hide-interaction-sound", true)
            );
            plugin.getLogger().info("Vanish ProtocolLib geliştirmesi etkin.");
        } else {
            packetEnhancer = new NoopVanishPacketEnhancer();
            if (protocolLibEnabled && protocolLib == null) {
                plugin.getLogger().info("ProtocolLib bulunamadı; gelişmiş vanish etkileşim gizleme devre dışı.");
            }
        }

        vanishService.setPacketEnhancer(packetEnhancer);
        if (isActive()) {
            packetEnhancer.enable();
        }
    }
}
