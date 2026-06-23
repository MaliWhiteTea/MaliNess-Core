package com.mertaliakcay.malinesscore.systems.economy;

import com.mertaliakcay.malinesscore.systems.economy.model.CurrencyDefinition;
import com.mertaliakcay.malinesscore.systems.economy.model.PlayerEconomyAccount;
import com.mertaliakcay.malinesscore.systems.economy.model.TransactionResult;
import com.mertaliakcay.malinesscore.systems.economy.storage.EconomyStorage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class EconomyService {

    private final CurrencyRegistry currencyRegistry;
    private final EconomySettings settings;
    private final EconomyStorage storage;
    private final EconomyFormatter formatter;
    private final EconomyTransactionLogger transactionLogger;
    private final Map<UUID, Object> locks = new ConcurrentHashMap<>();
    private volatile boolean available;

    public EconomyService(
            CurrencyRegistry currencyRegistry,
            EconomySettings settings,
            EconomyStorage storage,
            EconomyFormatter formatter,
            EconomyTransactionLogger transactionLogger
    ) {
        this.currencyRegistry = currencyRegistry;
        this.settings = settings;
        this.storage = storage;
        this.formatter = formatter;
        this.transactionLogger = transactionLogger;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public boolean isAvailable() {
        return available;
    }

    public CurrencyRegistry getCurrencyRegistry() {
        return currencyRegistry;
    }

    public EconomySettings getSettings() {
        return settings;
    }

    public EconomyFormatter getFormatter() {
        return formatter;
    }

    public BigDecimal getBalance(UUID accountId, String currencyId) {
        if (!available) {
            return BigDecimal.ZERO;
        }
        PlayerEconomyAccount account = storage.load(accountId);
        return currencyRegistry.normalize(currencyId, account.getBalance(currencyId));
    }

    public BigDecimal getBalance(OfflinePlayer player, String currencyId) {
        return getBalance(player.getUniqueId(), currencyId);
    }

    public TransactionResult setBalance(UUID accountId, String currencyId, BigDecimal amount, String source) {
        if (!available) {
            return TransactionResult.ECONOMY_UNAVAILABLE;
        }
        CurrencyDefinition currency = currencyRegistry.get(currencyId).orElse(null);
        if (currency == null) {
            return TransactionResult.CURRENCY_NOT_FOUND;
        }

        BigDecimal normalized = currencyRegistry.normalize(currencyId, amount);
        if (normalized.compareTo(BigDecimal.ZERO) < 0) {
            return TransactionResult.INVALID_AMOUNT;
        }
        if (normalized.compareTo(currency.getMaxBalance()) > 0) {
            return TransactionResult.LIMIT_EXCEEDED;
        }

        synchronized (lock(accountId)) {
            PlayerEconomyAccount account = storage.load(accountId);
            BigDecimal previous = account.getBalance(currencyId);
            account.setBalance(currencyId, normalized);
            storage.saveAsync(accountId, account);
            transactionLogger.log(
                    "SET",
                    currencyId,
                    label(accountId),
                    label(accountId),
                    formatter.format(normalized, currencyId),
                    "set-balance",
                    source
            );
            if (previous.compareTo(normalized) != 0) {
                transactionLogger.log(
                        "ADJUST",
                        currencyId,
                        label(accountId),
                        label(accountId),
                        formatter.format(normalized.subtract(previous), currencyId),
                        "delta",
                        source
                );
            }
        }
        return TransactionResult.SUCCESS;
    }

    public TransactionResult deposit(UUID accountId, String currencyId, BigDecimal amount, String reason, String source) {
        return mutate(accountId, currencyId, amount, true, reason, source);
    }

    public TransactionResult withdraw(UUID accountId, String currencyId, BigDecimal amount, String reason, String source) {
        return mutate(accountId, currencyId, amount, false, reason, source);
    }

    public TransactionResult transfer(
            UUID fromId,
            UUID toId,
            String currencyId,
            BigDecimal amount,
            String reason,
            String source
    ) {
        if (!available) {
            return TransactionResult.ECONOMY_UNAVAILABLE;
        }
        if (fromId.equals(toId)) {
            return TransactionResult.SAME_ACCOUNT;
        }

        CurrencyDefinition currency = currencyRegistry.get(currencyId).orElse(null);
        if (currency == null) {
            return TransactionResult.CURRENCY_NOT_FOUND;
        }

        BigDecimal normalized = validateActionAmount(currencyId, amount);
        if (normalized == null) {
            return TransactionResult.INVALID_AMOUNT;
        }

        UUID first = fromId.compareTo(toId) < 0 ? fromId : toId;
        UUID second = fromId.compareTo(toId) < 0 ? toId : fromId;

        synchronized (lock(first)) {
            synchronized (lock(second)) {
                PlayerEconomyAccount fromAccount = storage.load(fromId);
                PlayerEconomyAccount toAccount = storage.load(toId);

                BigDecimal fromBalance = currencyRegistry.normalize(currencyId, fromAccount.getBalance(currencyId));
                if (fromBalance.compareTo(normalized) < 0) {
                    return TransactionResult.INSUFFICIENT_FUNDS;
                }

                BigDecimal toBalance = currencyRegistry.normalize(currencyId, toAccount.getBalance(currencyId));
                BigDecimal newToBalance = toBalance.add(normalized);
                if (newToBalance.compareTo(currency.getMaxBalance()) > 0) {
                    return TransactionResult.LIMIT_EXCEEDED;
                }

                fromAccount.setBalance(currencyId, fromBalance.subtract(normalized));
                toAccount.setBalance(currencyId, newToBalance);
                storage.saveAsync(fromId, fromAccount);
                storage.saveAsync(toId, toAccount);

                transactionLogger.log(
                        "TRANSFER",
                        currencyId,
                        label(fromId),
                        label(toId),
                        formatter.format(normalized, currencyId),
                        reason,
                        source
                );
            }
        }
        return TransactionResult.SUCCESS;
    }

    public BigDecimal calculatePayTax(BigDecimal amount) {
        if (!settings.isPayTaxEnabled()) {
            return BigDecimal.ZERO;
        }
        BigDecimal percentTax = amount.multiply(settings.getPayTaxPercent())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return percentTax.add(settings.getPayTaxFlat()).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal clampPayAmount(BigDecimal amount) {
        CurrencyDefinition currency = currencyRegistry.getPrimary();
        if (currency == null || amount == null) {
            return null;
        }
        BigDecimal normalized = currencyRegistry.normalize(currency.getId(), amount);
        if (normalized.compareTo(settings.getMinPayAmount()) < 0
                || normalized.compareTo(settings.getMaxPayAmount()) > 0) {
            return null;
        }
        return normalized;
    }

    public BigDecimal validateActionAmount(String currencyId, BigDecimal amount) {
        CurrencyDefinition currency = currencyRegistry.get(currencyId).orElse(null);
        if (currency == null || amount == null) {
            return null;
        }
        BigDecimal normalized = currencyRegistry.normalize(currencyId, amount);
        if (normalized.compareTo(settings.getMinActionAmount()) < 0) {
            return null;
        }
        if (normalized.compareTo(settings.getMaxActionAmount()) > 0) {
            normalized = settings.getMaxActionAmount();
        }
        return normalized;
    }

    public String format(BigDecimal amount, String currencyId) {
        return formatter.format(amount, currencyId);
    }

    public String formatPrimary(BigDecimal amount) {
        return formatter.formatPrimary(amount);
    }

    public void flushAll() {
        storage.flushAll();
    }

    private TransactionResult mutate(
            UUID accountId,
            String currencyId,
            BigDecimal amount,
            boolean deposit,
            String reason,
            String source
    ) {
        if (!available) {
            return TransactionResult.ECONOMY_UNAVAILABLE;
        }

        CurrencyDefinition currency = currencyRegistry.get(currencyId).orElse(null);
        if (currency == null) {
            return TransactionResult.CURRENCY_NOT_FOUND;
        }

        BigDecimal normalized = validateActionAmount(currencyId, amount);
        if (normalized == null) {
            return TransactionResult.INVALID_AMOUNT;
        }

        synchronized (lock(accountId)) {
            PlayerEconomyAccount account = storage.load(accountId);
            BigDecimal balance = currencyRegistry.normalize(currencyId, account.getBalance(currencyId));
            BigDecimal updated = deposit ? balance.add(normalized) : balance.subtract(normalized);
            if (!deposit && updated.compareTo(BigDecimal.ZERO) < 0) {
                return TransactionResult.INSUFFICIENT_FUNDS;
            }
            if (updated.compareTo(currency.getMaxBalance()) > 0) {
                return TransactionResult.LIMIT_EXCEEDED;
            }

            account.setBalance(currencyId, updated);
            storage.saveAsync(accountId, account);
            transactionLogger.log(
                    deposit ? "DEPOSIT" : "WITHDRAW",
                    currencyId,
                    deposit ? "SERVER" : label(accountId),
                    deposit ? label(accountId) : "SERVER",
                    formatter.format(normalized, currencyId),
                    reason,
                    source
            );
        }
        return TransactionResult.SUCCESS;
    }

    private Object lock(UUID accountId) {
        return locks.computeIfAbsent(accountId, ignored -> new Object());
    }

    private String label(UUID accountId) {
        if (EconomyConstants.SERVER_ACCOUNT_ID.equals(accountId)) {
            return "SERVER";
        }
        OfflinePlayer player = Bukkit.getOfflinePlayer(accountId);
        String name = player.getName();
        return name != null ? name : accountId.toString();
    }
}
