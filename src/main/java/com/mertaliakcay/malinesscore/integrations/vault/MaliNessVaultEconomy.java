package com.mertaliakcay.malinesscore.integrations.vault;

import com.mertaliakcay.malinesscore.systems.economy.EconomyConstants;
import com.mertaliakcay.malinesscore.systems.economy.EconomyService;
import com.mertaliakcay.malinesscore.systems.economy.model.CurrencyDefinition;
import com.mertaliakcay.malinesscore.systems.economy.model.TransactionResult;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class MaliNessVaultEconomy implements Economy {

    private final EconomyService economyService;

    public MaliNessVaultEconomy(EconomyService economyService) {
        this.economyService = economyService;
    }

    @Override
    public boolean isEnabled() {
        return economyService.isAvailable();
    }

    @Override
    public String getName() {
        return "MaliNess Economy";
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        CurrencyDefinition currency = economyService.getCurrencyRegistry().getPrimary();
        return currency != null ? currency.getDecimalPlaces() : 2;
    }

    @Override
    public String format(double amount) {
        return economyService.formatPrimary(BigDecimal.valueOf(amount));
    }

    @Override
    public String currencyNamePlural() {
        CurrencyDefinition currency = economyService.getCurrencyRegistry().getPrimary();
        return currency != null ? currency.getDisplayName() : "TL";
    }

    @Override
    public String currencyNameSingular() {
        return currencyNamePlural();
    }

    @Override
    public boolean hasAccount(String playerName) {
        return Bukkit.getOfflinePlayer(playerName).hasPlayedBefore() || Bukkit.getPlayerExact(playerName) != null;
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return player != null;
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return hasAccount(playerName);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return hasAccount(player);
    }

    @Override
    public double getBalance(String playerName) {
        return getBalance(Bukkit.getOfflinePlayer(playerName));
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return economyService.getBalance(player.getUniqueId(), EconomyConstants.PRIMARY_CURRENCY).doubleValue();
    }

    @Override
    public double getBalance(String playerName, String world) {
        return getBalance(playerName);
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player);
    }

    @Override
    public boolean has(String playerName, double amount) {
        return getBalance(playerName) >= amount;
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }

    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return has(playerName, amount);
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        return mutate(Bukkit.getOfflinePlayer(playerName).getUniqueId(), amount, false);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        return mutate(player.getUniqueId(), amount, false);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        return mutate(Bukkit.getOfflinePlayer(playerName).getUniqueId(), amount, true);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        return mutate(player.getUniqueId(), amount, true);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return depositPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player, amount);
    }

    @Override
    public boolean createPlayerAccount(String playerName) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return true;
    }

    @Override
    public EconomyResponse createBank(String name, String player) {
        return notImplemented();
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return notImplemented();
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        return notImplemented();
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        return notImplemented();
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return notImplemented();
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return notImplemented();
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return notImplemented();
    }

    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        return notImplemented();
    }

    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        return notImplemented();
    }

    @Override
    public List<String> getBanks() {
        return Collections.emptyList();
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return notImplemented();
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return notImplemented();
    }

    private EconomyResponse mutate(UUID accountId, double amount, boolean deposit) {
        if (amount < 0) {
            return failure("Negatif miktar");
        }
        BigDecimal value = BigDecimal.valueOf(amount);
        TransactionResult result = deposit
                ? economyService.deposit(accountId, EconomyConstants.PRIMARY_CURRENCY, value, "vault-api", "vault")
                : economyService.withdraw(accountId, EconomyConstants.PRIMARY_CURRENCY, value, "vault-api", "vault");
        double balance = economyService.getBalance(accountId, EconomyConstants.PRIMARY_CURRENCY).doubleValue();
        return switch (result) {
            case SUCCESS -> new EconomyResponse(amount, balance, EconomyResponse.ResponseType.SUCCESS, null);
            case INSUFFICIENT_FUNDS -> failure("Yetersiz bakiye", balance);
            case LIMIT_EXCEEDED -> failure("Limit asildi", balance);
            default -> failure(result.name(), balance);
        };
    }

    private EconomyResponse failure(String message) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, message);
    }

    private EconomyResponse failure(String message, double balance) {
        return new EconomyResponse(0, balance, EconomyResponse.ResponseType.FAILURE, message);
    }

    private EconomyResponse notImplemented() {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank desteklenmiyor");
    }
}
