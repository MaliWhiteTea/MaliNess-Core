package com.mertaliakcay.malinesscore.systems.economy.model;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerEconomyAccount {

    private final UUID playerId;
    private final Map<String, BigDecimal> balances = new HashMap<>();

    public PlayerEconomyAccount(UUID playerId) {
        this.playerId = playerId;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public Map<String, BigDecimal> getBalances() {
        return balances;
    }

    public BigDecimal getBalance(String currencyId) {
        return balances.getOrDefault(currencyId, BigDecimal.ZERO);
    }

    public void setBalance(String currencyId, BigDecimal amount) {
        balances.put(currencyId, amount);
    }
}
