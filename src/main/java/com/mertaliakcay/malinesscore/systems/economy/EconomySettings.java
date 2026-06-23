package com.mertaliakcay.malinesscore.systems.economy;

import org.bukkit.configuration.file.FileConfiguration;

import java.math.BigDecimal;

public final class EconomySettings {

    private boolean transactionLogging;
    private BigDecimal defaultStartingBalance;
    private BigDecimal minPayAmount;
    private BigDecimal maxPayAmount;
    private boolean payConfirmationEnabled;
    private BigDecimal payConfirmationThreshold;
    private boolean payTaxEnabled;
    private BigDecimal payTaxPercent;
    private BigDecimal payTaxFlat;
    private BigDecimal minActionAmount;
    private BigDecimal maxActionAmount;

    public void load(FileConfiguration config) {
        transactionLogging = config.getBoolean("logging.transactions", true);
        defaultStartingBalance = BigDecimal.valueOf(config.getDouble("starting-balance", 0D));

        minPayAmount = BigDecimal.valueOf(config.getDouble("pay.min-amount", 0.01D));
        maxPayAmount = BigDecimal.valueOf(config.getDouble("pay.max-amount", 10_000_000D));
        payConfirmationEnabled = config.getBoolean("pay.confirmation.enabled", true);
        payConfirmationThreshold = BigDecimal.valueOf(config.getDouble(
                "pay.confirmation.threshold",
                config.getDouble("pay.confirmation-threshold", 1_000_000D)
        ));

        payTaxEnabled = config.getBoolean("pay.tax.enabled", false);
        payTaxPercent = BigDecimal.valueOf(config.getDouble("pay.tax.percent", 0D));
        payTaxFlat = BigDecimal.valueOf(config.getDouble("pay.tax.flat", 0D));

        minActionAmount = BigDecimal.valueOf(config.getDouble("limits.min-amount", 0.01D));
        maxActionAmount = BigDecimal.valueOf(config.getDouble("limits.max-per-action", 1_000_000D));
    }

    public boolean isTransactionLogging() {
        return transactionLogging;
    }

    public BigDecimal getDefaultStartingBalance() {
        return defaultStartingBalance;
    }

    public BigDecimal getMinPayAmount() {
        return minPayAmount;
    }

    public BigDecimal getMaxPayAmount() {
        return maxPayAmount;
    }

    public BigDecimal getPayConfirmationThreshold() {
        return payConfirmationThreshold;
    }

    public boolean isPayConfirmationEnabled() {
        return payConfirmationEnabled;
    }

    public boolean isPayTaxEnabled() {
        return payTaxEnabled;
    }

    public BigDecimal getPayTaxPercent() {
        return payTaxPercent;
    }

    public BigDecimal getPayTaxFlat() {
        return payTaxFlat;
    }

    public BigDecimal getMinActionAmount() {
        return minActionAmount;
    }

    public BigDecimal getMaxActionAmount() {
        return maxActionAmount;
    }
}
