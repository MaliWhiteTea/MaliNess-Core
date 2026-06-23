package com.mertaliakcay.malinesscore.systems.economy.command;

import com.mertaliakcay.malinesscore.MaliNessCore;
import com.mertaliakcay.malinesscore.confirmation.ConfirmationService;
import com.mertaliakcay.malinesscore.systems.economy.EconomyConstants;
import com.mertaliakcay.malinesscore.systems.economy.EconomyFormatter;
import com.mertaliakcay.malinesscore.systems.economy.EconomySystem;
import com.mertaliakcay.malinesscore.systems.economy.EconomyService;
import com.mertaliakcay.malinesscore.systems.economy.model.CurrencyDefinition;
import com.mertaliakcay.malinesscore.systems.economy.model.TransactionResult;
import com.mertaliakcay.malinesscore.util.CommandSuggestions;
import com.mertaliakcay.malinesscore.util.SystemLang;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

public final class PayCommand {

    private final MaliNessCore plugin;
    private final EconomyService economyService;
    private final SystemLang lang;

    public PayCommand(MaliNessCore plugin, EconomyService economyService, SystemLang lang) {
        this.plugin = plugin;
        this.economyService = economyService;
        this.lang = lang;
    }

    public void handle(Player sender, String[] args) {
        if (!economyService.isAvailable()) {
            lang.send(sender, "economy-unavailable");
            return;
        }

        if (!sender.hasPermission(EconomySystem.PERM_PAY)) {
            lang.send(sender, "no-permission");
            return;
        }

        if (args.length < 2) {
            lang.send(sender, "pay-usage");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target.getName() == null && !target.hasPlayedBefore()) {
            lang.send(sender, "player-not-found", "player", args[0]);
            return;
        }

        if (sender.getUniqueId().equals(target.getUniqueId())) {
            lang.send(sender, "pay-self");
            return;
        }

        CurrencyDefinition currency = economyService.getCurrencyRegistry().getPrimary();
        if (currency == null || !currency.isPlayerToPlayer()) {
            lang.send(sender, "currency-disabled");
            return;
        }

        BigDecimal amount = EconomyFormatter.parseAmount(args[1], currency.getDecimalPlaces());
        amount = economyService.clampPayAmount(amount);
        if (amount == null) {
            lang.send(sender, "pay-invalid-amount",
                    "min", economyService.format(economyService.getSettings().getMinPayAmount(), currency.getId()),
                    "max", economyService.format(economyService.getSettings().getMaxPayAmount(), currency.getId()));
            return;
        }

        BigDecimal tax = economyService.calculatePayTax(amount);
        BigDecimal total = amount.add(tax);
        BigDecimal balance = economyService.getBalance(sender, currency.getId());
        if (balance.compareTo(total) < 0) {
            sendInsufficientFunds(sender, currency.getId(), total, balance);
            return;
        }

        boolean targetOnline = target.isOnline();
        boolean needsLargeConfirm = economyService.getSettings().isPayConfirmationEnabled()
                && amount.compareTo(economyService.getSettings().getPayConfirmationThreshold()) >= 0;
        if (!targetOnline || needsLargeConfirm) {
            requestConfirmation(sender, target, amount, tax, targetOnline, needsLargeConfirm);
            return;
        }

        executePay(sender, target, amount, tax);
    }

    public List<String> suggest(Player sender, String[] args) {
        if (args.length == 0) {
            return CommandSuggestions.onlinePlayerNames(sender, "", true);
        }
        if (args.length == 1) {
            return CommandSuggestions.onlinePlayerNames(sender, args[0], true);
        }
        if (args.length == 2) {
            return List.of("10", "50", "100", "500", "1000");
        }
        return List.of();
    }

    private void sendInsufficientFunds(Player sender, String currencyId, BigDecimal required, BigDecimal balance) {
        BigDecimal shortfall = required.subtract(balance).max(BigDecimal.ZERO);
        lang.send(sender, "insufficient-funds",
                "shortfall", economyService.format(shortfall, currencyId),
                "balance", economyService.format(balance, currencyId));
    }

    private void requestConfirmation(
            Player sender,
            OfflinePlayer target,
            BigDecimal amount,
            BigDecimal tax,
            boolean targetOnline,
            boolean largeAmount
    ) {
        String targetName = target.getName() != null ? target.getName() : sender.getName();
        String key = !targetOnline ? "pay-confirm-offline" : (largeAmount ? "pay-confirm-large" : "pay-confirm");
        ConfirmationService confirmationService = plugin.getConfirmationService();
        confirmationService.request(
                sender,
                lang.getPlain(key, sender,
                        "target", targetName,
                        "amount", economyService.format(amount, EconomyConstants.PRIMARY_CURRENCY),
                        "tax", economyService.format(tax, EconomyConstants.PRIMARY_CURRENCY)),
                () -> executePay(sender, target, amount, tax),
                null
        );
    }

    private void executePay(Player sender, OfflinePlayer target, BigDecimal amount, BigDecimal tax) {
        CurrencyDefinition currency = economyService.getCurrencyRegistry().getPrimary();
        BigDecimal total = amount.add(tax);

        TransactionResult withdrawResult = economyService.withdraw(
                sender.getUniqueId(),
                currency.getId(),
                total,
                "pay",
                "command-pay"
        );
        if (withdrawResult != TransactionResult.SUCCESS) {
            lang.send(sender, "pay-failed", "reason", withdrawResult.name());
            return;
        }

        TransactionResult depositResult = economyService.deposit(
                target.getUniqueId(),
                currency.getId(),
                amount,
                "pay",
                "command-pay"
        );
        if (depositResult != TransactionResult.SUCCESS) {
            economyService.deposit(sender.getUniqueId(), currency.getId(), total, "pay-rollback", "command-pay");
            lang.send(sender, "pay-failed", "reason", depositResult.name());
            return;
        }

        if (tax.compareTo(BigDecimal.ZERO) > 0) {
            economyService.deposit(
                    EconomyConstants.SERVER_ACCOUNT_ID,
                    currency.getId(),
                    tax,
                    "pay-tax",
                    "command-pay"
            );
        }

        String targetName = target.getName() != null ? target.getName() : target.getUniqueId().toString();
        lang.send(sender, "pay-sent",
                "target", targetName,
                "amount", economyService.format(amount, currency.getId()));
        if (target.isOnline() && target.getPlayer() != null) {
            lang.send(target.getPlayer(), "pay-received",
                    "sender", sender.getName(),
                    "amount", economyService.format(amount, currency.getId()));
        }
    }
}
