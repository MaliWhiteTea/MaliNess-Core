package com.mertaliakcay.malinesscore.systems.economy.model;

import java.math.BigDecimal;

public final class CurrencyDefinition {

    private final String id;
    private final String displayName;
    private final String symbol;
    private final String format;
    private final int decimalPlaces;
    private final boolean vaultPrimary;
    private final boolean playerToPlayer;
    private final BigDecimal defaultBalance;
    private final BigDecimal maxBalance;

    public CurrencyDefinition(
            String id,
            String displayName,
            String symbol,
            String format,
            int decimalPlaces,
            boolean vaultPrimary,
            boolean playerToPlayer,
            BigDecimal defaultBalance,
            BigDecimal maxBalance
    ) {
        this.id = id;
        this.displayName = displayName;
        this.symbol = symbol;
        this.format = format;
        this.decimalPlaces = decimalPlaces;
        this.vaultPrimary = vaultPrimary;
        this.playerToPlayer = playerToPlayer;
        this.defaultBalance = defaultBalance;
        this.maxBalance = maxBalance;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getFormat() {
        return format;
    }

    public int getDecimalPlaces() {
        return decimalPlaces;
    }

    public boolean isVaultPrimary() {
        return vaultPrimary;
    }

    public boolean isPlayerToPlayer() {
        return playerToPlayer;
    }

    public BigDecimal getDefaultBalance() {
        return defaultBalance;
    }

    public BigDecimal getMaxBalance() {
        return maxBalance;
    }
}
