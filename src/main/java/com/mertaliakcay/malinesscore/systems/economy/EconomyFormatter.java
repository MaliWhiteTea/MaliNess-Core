package com.mertaliakcay.malinesscore.systems.economy;

import com.mertaliakcay.malinesscore.systems.economy.model.CurrencyDefinition;
import org.bukkit.configuration.file.FileConfiguration;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class EconomyFormatter {

    private final CurrencyRegistry currencyRegistry;

    public EconomyFormatter(CurrencyRegistry currencyRegistry) {
        this.currencyRegistry = currencyRegistry;
    }

    public String format(BigDecimal amount, String currencyId) {
        CurrencyDefinition currency = currencyRegistry.get(currencyId).orElse(currencyRegistry.getPrimary());
        if (currency == null || amount == null) {
            return "0";
        }

        BigDecimal normalized = amount.setScale(currency.getDecimalPlaces(), RoundingMode.HALF_UP);
        String amountText = normalized.stripTrailingZeros().scale() <= 0
                ? normalized.toBigInteger().toString()
                : normalized.toPlainString();

        return currency.getFormat()
                .replace("{amount}", amountText)
                .replace("{name}", currency.getDisplayName())
                .replace("{symbol}", currency.getSymbol());
    }

    public String formatPrimary(BigDecimal amount) {
        return format(amount, currencyRegistry.getPrimaryCurrencyId());
    }

    public static BigDecimal parseAmount(String raw, int decimalPlaces) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String normalized = raw.trim().replace(',', '.');
        try {
            BigDecimal value = new BigDecimal(normalized);
            return value.setScale(decimalPlaces, RoundingMode.HALF_UP);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    public static boolean isPositive(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public static boolean isZeroOrNegative(BigDecimal amount) {
        return amount == null || amount.compareTo(BigDecimal.ZERO) <= 0;
    }
}
