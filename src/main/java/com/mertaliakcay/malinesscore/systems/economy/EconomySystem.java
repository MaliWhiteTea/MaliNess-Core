package com.mertaliakcay.malinesscore.systems.economy;

import com.mertaliakcay.malinesscore.integrations.vault.VaultIntegration;
import com.mertaliakcay.malinesscore.systems.AbstractGameSystem;
import com.mertaliakcay.malinesscore.systems.economy.command.BalanceCommand;
import com.mertaliakcay.malinesscore.systems.economy.command.EconomyBasicCommand;
import com.mertaliakcay.malinesscore.systems.economy.command.EconomyMnCommand;
import com.mertaliakcay.malinesscore.systems.economy.command.PayCommand;
import com.mertaliakcay.malinesscore.systems.economy.storage.YamlEconomyStorage;
import org.bukkit.entity.Player;

import java.util.List;

public final class EconomySystem extends AbstractGameSystem {

    public static final String PERM_PAY = "maliness-core.economy.pay";
    public static final String PERM_BALANCE = "maliness-core.economy.balance";
    public static final String PERM_BALANCE_OTHERS = "maliness-core.economy.balance.others";
    public static final String PERM_ADMIN = "maliness-core.economy.admin";

    private CurrencyRegistry currencyRegistry;
    private EconomySettings economySettings;
    private EconomyService economyService;
    private VaultIntegration vaultIntegration;
    private YamlEconomyStorage storage;
    private PayCommand payCommand;
    private BalanceCommand balanceCommand;
    private EconomyMnCommand economyMnCommand;

    @Override
    protected String getSystemId() {
        return "economy";
    }

    @Override
    public boolean isConfigEnabled() {
        return super.isConfigEnabled()
                && plugin.getConfig().getBoolean("integrations.vault.enabled", true)
                && VaultIntegration.isVaultPluginPresent(plugin);
    }

    @Override
    protected void onRegister() {
        reloadServices();

        payCommand = new PayCommand(plugin, economyService, lang);
        balanceCommand = new BalanceCommand(economyService, lang);
        economyMnCommand = new EconomyMnCommand(economyService, lang);

        registerLifecycleCommandsOnce(registrar -> {
            EconomyBasicCommand payWrapper = new EconomyBasicCommand(
                    (sender, commandArgs) -> {
                        if (sender instanceof Player player) {
                            payCommand.handle(player, commandArgs);
                        }
                    },
                    (sender, commandArgs) -> sender instanceof Player player ? payCommand.suggest(player, commandArgs) : List.of()
            );
            EconomyBasicCommand balanceWrapper = new EconomyBasicCommand(
                    (sender, commandArgs) -> balanceCommand.handle(sender, commandArgs),
                    (sender, commandArgs) -> balanceCommand.suggest(sender, commandArgs)
            );
            EconomyBasicCommand ecoWrapper = new EconomyBasicCommand(
                    (sender, commandArgs) -> economyMnCommand.handle(sender, commandArgs),
                    (sender, commandArgs) -> economyMnCommand.suggest(sender, commandArgs)
            );

            registrar.register("pay", "Oyuncuya para gönderir.", List.of("paragonder", "paragönder"), payWrapper);
            registrar.register("para", "Bakiyeni gösterir.", List.of("bal", "balance", "bakiye"), balanceWrapper);
            registrar.register("eco", "Ekonomi yönetim komutları.", List.of(), ecoWrapper);
        });

        plugin.getMalinessCommand().setEconomy(this, economyMnCommand);
        registerListener(vaultIntegration);
    }

    @Override
    protected void onEnable() {
        if (!VaultIntegration.isVaultPluginPresent(plugin) && config.get().getBoolean("enabled", true)) {
            plugin.getLogger().warning("[Economy] Vault yüklü değil — economy sistemi devre dışı bırakıldı.");
        }
    }

    @Override
    protected void onActivate() {
        vaultIntegration.enable();
        storage.preloadOnlinePlayers();
    }

    @Override
    protected void onDeactivate() {
        if (vaultIntegration != null) {
            vaultIntegration.disable();
        }
    }

    @Override
    protected void onDisable() {
        if (economyService != null) {
            economyService.flushAll();
        }
    }

    @Override
    protected void onUnregister() {
        plugin.getMalinessCommand().clearEconomy();
    }

    public EconomyService getEconomyService() {
        return economyService;
    }

    public boolean isVaultMissing() {
        return !VaultIntegration.isVaultPluginPresent(plugin);
    }

    public void activateIfVaultAvailable() {
        if (!config.get().getBoolean("enabled", true)) {
            return;
        }
        if (!VaultIntegration.isVaultPluginPresent(plugin)) {
            return;
        }
        if (isActive()) {
            vaultIntegration.enable();
            return;
        }
        if (!isConfigEnabled()) {
            return;
        }
        activateIfInactive();
    }

    private void reloadServices() {
        currencyRegistry = new CurrencyRegistry();
        currencyRegistry.load(config.get());

        economySettings = new EconomySettings();
        economySettings.load(config.get());

        EconomyFormatter formatter = new EconomyFormatter(currencyRegistry);
        EconomyTransactionLogger transactionLogger = new EconomyTransactionLogger(plugin, economySettings.isTransactionLogging());

        if (storage == null) {
            storage = new YamlEconomyStorage(plugin, currencyRegistry, economySettings);
        }

        economyService = new EconomyService(currencyRegistry, economySettings, storage, formatter, transactionLogger);
        if (vaultIntegration == null) {
            vaultIntegration = new VaultIntegration(plugin, economyService);
        } else {
            vaultIntegration.setEconomyService(economyService);
        }
    }
}
