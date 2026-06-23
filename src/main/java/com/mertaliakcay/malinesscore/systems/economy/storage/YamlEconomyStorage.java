package com.mertaliakcay.malinesscore.systems.economy.storage;

import com.mertaliakcay.malinesscore.MaliNessCore;
import com.mertaliakcay.malinesscore.systems.economy.CurrencyRegistry;
import com.mertaliakcay.malinesscore.systems.economy.EconomySettings;
import com.mertaliakcay.malinesscore.systems.economy.model.CurrencyDefinition;
import com.mertaliakcay.malinesscore.systems.economy.model.PlayerEconomyAccount;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class YamlEconomyStorage implements EconomyStorage {

    private final MaliNessCore plugin;
    private final File accountsFolder;
    private final CurrencyRegistry currencyRegistry;
    private final EconomySettings settings;
    private final Map<UUID, PlayerEconomyAccount> cache = new ConcurrentHashMap<>();

    public YamlEconomyStorage(MaliNessCore plugin, CurrencyRegistry currencyRegistry, EconomySettings settings) {
        this.plugin = plugin;
        this.currencyRegistry = currencyRegistry;
        this.settings = settings;
        this.accountsFolder = new File(plugin.getDataFolder(), "data/economy/accounts");
        if (!accountsFolder.exists()) {
            accountsFolder.mkdirs();
        }
    }

    @Override
    public PlayerEconomyAccount load(UUID playerId) {
        return cache.computeIfAbsent(playerId, this::loadFromDisk);
    }

    @Override
    public CompletableFuture<Void> saveAsync(UUID playerId, PlayerEconomyAccount account) {
        cache.put(playerId, account);
        return CompletableFuture.runAsync(() -> saveToDisk(playerId, account));
    }

    @Override
    public void flushAll() {
        for (Map.Entry<UUID, PlayerEconomyAccount> entry : cache.entrySet()) {
            saveToDisk(entry.getKey(), entry.getValue());
        }
    }

    private PlayerEconomyAccount loadFromDisk(UUID playerId) {
        PlayerEconomyAccount account = new PlayerEconomyAccount(playerId);
        File file = getFile(playerId);
        if (!file.exists()) {
            initializeDefaults(account);
            saveToDisk(playerId, account);
            return account;
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection balances = yaml.getConfigurationSection("balances");
        if (balances != null) {
            for (String currencyId : balances.getKeys(false)) {
                account.setBalance(currencyId, BigDecimal.valueOf(balances.getDouble(currencyId)));
            }
        }

        for (CurrencyDefinition currency : currencyRegistry.getAll()) {
            if (!account.getBalances().containsKey(currency.getId())) {
                account.setBalance(currency.getId(), currency.getDefaultBalance());
            }
        }
        return account;
    }

    private void initializeDefaults(PlayerEconomyAccount account) {
        for (CurrencyDefinition currency : currencyRegistry.getAll()) {
            BigDecimal starting = currency.getId().equals(currencyRegistry.getPrimaryCurrencyId())
                    ? settings.getDefaultStartingBalance()
                    : currency.getDefaultBalance();
            account.setBalance(currency.getId(), starting);
        }
    }

    private void saveToDisk(UUID playerId, PlayerEconomyAccount account) {
        File file = getFile(playerId);
        File temp = new File(file.getParentFile(), file.getName() + ".tmp");
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<String, BigDecimal> entry : account.getBalances().entrySet()) {
            yaml.set("balances." + entry.getKey(), entry.getValue().doubleValue());
        }

        try {
            yaml.save(temp);
            Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException exception) {
            try {
                Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException retryException) {
                plugin.getLogger().log(Level.SEVERE, "Economy hesabi kaydedilemedi: " + playerId, retryException);
            }
        }
    }

    private File getFile(UUID playerId) {
        return new File(accountsFolder, playerId + ".yml");
    }

    public void preloadOnlinePlayers() {
        Bukkit.getOnlinePlayers().forEach(player -> load(player.getUniqueId()));
    }
}
