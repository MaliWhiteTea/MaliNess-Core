package com.mertaliakcay.malinesscore.systems.economy.storage;

import com.mertaliakcay.malinesscore.systems.economy.model.PlayerEconomyAccount;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface EconomyStorage {

    PlayerEconomyAccount load(UUID playerId);

    CompletableFuture<Void> saveAsync(UUID playerId, PlayerEconomyAccount account);

    void flushAll();
}
