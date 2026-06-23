package com.mertaliakcay.malinesscore.systems.economy;

import com.mertaliakcay.malinesscore.systems.economy.model.CurrencyDefinition;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class CurrencyRegistry {

    private final Map<String, CurrencyDefinition> currencies = new LinkedHashMap<>();
    private String primaryCurrencyId = EconomyConstants.PRIMARY_CURRENCY;

    public void load(FileConfiguration config) {
        currencies.clear();
        ConfigurationSection section = config.getConfigurationSection("currencies");
        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            ConfigurationSection currencySection = section.getConfigurationSection(key);
            if (currencySection == null) {
                continue;
            }

            String id = currencySection.getString("id", key).toLowerCase(Locale.ROOT);
            CurrencyDefinition definition = new CurrencyDefinition(
                    id,
                    currencySection.getString("display-name", id),
                    currencySection.getString("symbol", ""),
                    currencySection.getString("format", "{amount} {name}"),
                    currencySection.getInt("decimal-places", 2),
                    currencySection.getBoolean("vault-primary", false),
                    currencySection.getBoolean("player-to-player", false),
                    BigDecimal.valueOf(currencySection.getDouble("default-balance", 0D)),
                    BigDecimal.valueOf(currencySection.getDouble("max-balance", 1_000_000_000D))
            );
            currencies.put(id, definition);
            if (definition.isVaultPrimary()) {
                primaryCurrencyId = id;
            }
        }
    }

    public Optional<CurrencyDefinition> get(String currencyId) {
        if (currencyId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(currencies.get(currencyId.toLowerCase(Locale.ROOT)));
    }

    public CurrencyDefinition getPrimary() {
        return currencies.get(primaryCurrencyId);
    }

    public String getPrimaryCurrencyId() {
        return primaryCurrencyId;
    }

    public Collection<CurrencyDefinition> getAll() {
        return currencies.values();
    }

    public boolean exists(String currencyId) {
        return get(currencyId).isPresent();
    }

    public BigDecimal normalize(String currencyId, BigDecimal amount) {
        CurrencyDefinition currency = get(currencyId).orElse(null);
        if (currency == null || amount == null) {
            return BigDecimal.ZERO;
        }
        return amount.setScale(currency.getDecimalPlaces(), RoundingMode.HALF_UP);
    }
}
