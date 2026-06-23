package com.mertaliakcay.malinesscore.integrations.vault;

import com.mertaliakcay.malinesscore.MaliNessCore;
import com.mertaliakcay.malinesscore.systems.economy.EconomySystem;
import com.mertaliakcay.malinesscore.systems.economy.EconomyService;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;

public final class VaultIntegration implements Listener {

    private final MaliNessCore plugin;
    private EconomyService economyService;
    private MaliNessVaultEconomy vaultEconomy;
    private boolean registered;

    public VaultIntegration(MaliNessCore plugin, EconomyService economyService) {
        this.plugin = plugin;
        this.economyService = economyService;
    }

    public void setEconomyService(EconomyService economyService) {
        disable();
        this.economyService = economyService;
        this.vaultEconomy = null;
    }

    public void enable() {
        if (!isVaultPluginPresent()) {
            economyService.setAvailable(false);
            plugin.getLogger().warning("[Economy] Vault yüklü değil — economy sistemi devre dışı bırakıldı.");
            return;
        }

        if (vaultEconomy == null) {
            vaultEconomy = new MaliNessVaultEconomy(economyService);
        }
        registerProvider();
        economyService.setAvailable(true);
    }

    public void disable() {
        unregisterProvider();
        economyService.setAvailable(false);
    }

    public boolean isVaultPresent() {
        return isVaultPluginPresent();
    }

    public boolean isRegistered() {
        return registered;
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        if (!"Vault".equalsIgnoreCase(event.getPlugin().getName())) {
            return;
        }
        if (vaultEconomy == null) {
            vaultEconomy = new MaliNessVaultEconomy(economyService);
        }
        registerProvider();
        economyService.setAvailable(true);
        EconomySystem economySystem = plugin.getEconomySystem();
        if (economySystem != null) {
            economySystem.activateIfVaultAvailable();
        }
    }

    private void registerProvider() {
        if (registered || vaultEconomy == null) {
            return;
        }
        plugin.getServer().getServicesManager().register(Economy.class, vaultEconomy, plugin, ServicePriority.Highest);
        registered = true;
        plugin.getLogger().info("[Economy] Vault Economy provider kaydedildi: MaliNess Economy");
    }

    private void unregisterProvider() {
        if (!registered || vaultEconomy == null) {
            return;
        }
        plugin.getServer().getServicesManager().unregister(Economy.class, vaultEconomy);
        registered = false;
    }

    private boolean isVaultPluginPresent() {
        Plugin vault = Bukkit.getPluginManager().getPlugin("Vault");
        return vault != null && vault.isEnabled();
    }

    public static boolean isVaultPluginPresent(MaliNessCore plugin) {
        Plugin vault = plugin.getServer().getPluginManager().getPlugin("Vault");
        return vault != null && vault.isEnabled();
    }

    public static boolean hasExternalEconomyProvider(MaliNessCore plugin) {
        RegisteredServiceProvider<Economy> provider = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        return provider != null && provider.getProvider() != null;
    }
}
