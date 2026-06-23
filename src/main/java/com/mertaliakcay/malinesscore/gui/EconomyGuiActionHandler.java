package com.mertaliakcay.malinesscore.gui;

import com.mertaliakcay.malinesscore.MaliNessCore;
import com.mertaliakcay.malinesscore.gui.model.EconomyOutcome;
import com.mertaliakcay.malinesscore.gui.model.MenuSession;
import com.mertaliakcay.malinesscore.systems.economy.EconomyConstants;
import com.mertaliakcay.malinesscore.systems.economy.EconomyService;
import com.mertaliakcay.malinesscore.systems.economy.model.TransactionResult;
import com.mertaliakcay.malinesscore.util.SystemLang;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.Locale;

public final class EconomyGuiActionHandler {

    private final MaliNessCore plugin;
    private final MenuService menuService;
    private final ChainActionRunner chainRunner;
    private final SystemLang economyLang;

    @FunctionalInterface
    public interface ChainActionRunner {
        void run(Player player, MenuSession session, String action);
    }

    public EconomyGuiActionHandler(
            MaliNessCore plugin,
            MenuService menuService,
            ChainActionRunner chainRunner,
            SystemLang economyLang
    ) {
        this.plugin = plugin;
        this.menuService = menuService;
        this.chainRunner = chainRunner;
        this.economyLang = economyLang;
    }

    public boolean handle(Player player, MenuSession session, String action) {
        EconomyService economyService = plugin.getEconomyService();
        if (economyService == null || !economyService.isAvailable()) {
            economyLang.send(player, "economy-unavailable");
            applyOutcome(player, session, session.getDefinition().getEconomyBehavior().getOnError());
            return true;
        }

        String[] chain = action.split(":then:", 2);
        String primary = chain[0].trim();
        String followUp = chain.length > 1 ? chain[1].trim() : null;

        if (primary.toLowerCase(Locale.ROOT).startsWith("economy:require:")) {
            return handleRequire(player, session, primary, followUp, economyService);
        }
        if (primary.toLowerCase(Locale.ROOT).startsWith("economy:withdraw:")) {
            return handleMutate(player, session, primary, followUp, economyService, false);
        }
        if (primary.toLowerCase(Locale.ROOT).startsWith("economy:deposit:")) {
            return handleMutate(player, session, primary, followUp, economyService, true);
        }
        if (primary.toLowerCase(Locale.ROOT).startsWith("economy:charge:")) {
            return handleMutate(player, session, primary.replace("charge:", "withdraw:"), followUp, economyService, false);
        }
        return false;
    }

    private boolean handleRequire(Player player, MenuSession session, String action, String followUp, EconomyService economyService) {
        BigDecimal amount = resolveAmount(action.substring("economy:require:".length()), session, economyService);
        if (amount == null) {
            economyLang.send(player, "gui-invalid-amount");
            applyOutcome(player, session, session.getDefinition().getEconomyBehavior().getOnError());
            return true;
        }

        BigDecimal balance = economyService.getBalance(player.getUniqueId(), EconomyConstants.PRIMARY_CURRENCY);
        if (balance.compareTo(amount) < 0) {
            sendInsufficientFunds(player, economyService, amount, balance);
            applyOutcome(player, session, session.getDefinition().getEconomyBehavior().getOnInsufficient());
            return true;
        }

        TransactionResult result = economyService.withdraw(
                player.getUniqueId(),
                EconomyConstants.PRIMARY_CURRENCY,
                amount,
                "gui-require",
                "gui"
        );
        if (result == TransactionResult.INSUFFICIENT_FUNDS) {
            sendInsufficientFunds(player, economyService, amount, balance);
            applyOutcome(player, session, session.getDefinition().getEconomyBehavior().getOnInsufficient());
            return true;
        }
        if (result != TransactionResult.SUCCESS) {
            economyLang.send(player, "gui-transaction-failed", "reason", result.name());
            applyOutcome(player, session, session.getDefinition().getEconomyBehavior().getOnError());
            return true;
        }

        finishSuccess(player, session, followUp, economyService, amount, false);
        return true;
    }

    private boolean handleMutate(
            Player player,
            MenuSession session,
            String action,
            String followUp,
            EconomyService economyService,
            boolean deposit
    ) {
        String prefix = deposit ? "economy:deposit:" : "economy:withdraw:";
        BigDecimal amount = resolveAmount(action.substring(prefix.length()), session, economyService);
        if (amount == null) {
            economyLang.send(player, "gui-invalid-amount");
            applyOutcome(player, session, session.getDefinition().getEconomyBehavior().getOnError());
            return true;
        }

        TransactionResult result = deposit
                ? economyService.deposit(player.getUniqueId(), EconomyConstants.PRIMARY_CURRENCY, amount, "gui-deposit", "gui")
                : economyService.withdraw(player.getUniqueId(), EconomyConstants.PRIMARY_CURRENCY, amount, "gui-withdraw", "gui");

        if (result == TransactionResult.SUCCESS) {
            finishSuccess(player, session, followUp, economyService, amount, deposit);
            return true;
        }

        if (result == TransactionResult.INSUFFICIENT_FUNDS) {
            BigDecimal balance = economyService.getBalance(player.getUniqueId(), EconomyConstants.PRIMARY_CURRENCY);
            sendInsufficientFunds(player, economyService, amount, balance);
            applyOutcome(player, session, session.getDefinition().getEconomyBehavior().getOnInsufficient());
            return true;
        }

        economyLang.send(player, "gui-transaction-failed", "reason", result.name());
        applyOutcome(player, session, session.getDefinition().getEconomyBehavior().getOnError());
        return true;
    }

    private void finishSuccess(
            Player player,
            MenuSession session,
            String followUp,
            EconomyService economyService,
            BigDecimal amount,
            boolean deposit
    ) {
        economyLang.send(player, deposit ? "gui-deposit-success" : "gui-withdraw-success",
                "amount", economyService.formatPrimary(amount));
        if (followUp != null && !followUp.isBlank()) {
            chainRunner.run(player, session, followUp);
            return;
        }
        applyOutcome(player, session, session.getDefinition().getEconomyBehavior().getOnSuccess());
    }

    private void sendInsufficientFunds(Player player, EconomyService economyService, BigDecimal required, BigDecimal balance) {
        BigDecimal shortfall = required.subtract(balance).max(BigDecimal.ZERO);
        economyLang.send(player, "insufficient-funds",
                "shortfall", economyService.formatPrimary(shortfall),
                "balance", economyService.formatPrimary(balance));
    }

    private BigDecimal resolveAmount(String raw, MenuSession session, EconomyService economyService) {
        String token = raw.trim();
        if (token.startsWith("{session:") && token.endsWith("}")) {
            String key = token.substring("{session:".length(), token.length() - 1);
            Object value = session.getProviderState().get(key);
            if (value instanceof Number number) {
                return economyService.validateActionAmount(EconomyConstants.PRIMARY_CURRENCY, BigDecimal.valueOf(number.doubleValue()));
            }
            if (value instanceof String stringValue) {
                return economyService.validateActionAmount(
                        EconomyConstants.PRIMARY_CURRENCY,
                        new BigDecimal(stringValue.replace(',', '.'))
                );
            }
            return null;
        }
        try {
            return economyService.validateActionAmount(
                    EconomyConstants.PRIMARY_CURRENCY,
                    new BigDecimal(token.replace(',', '.'))
            );
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private void applyOutcome(Player player, MenuSession session, EconomyOutcome outcome) {
        if (outcome == EconomyOutcome.CLOSE) {
            menuService.close(player, true);
        } else {
            menuService.refreshView(player, session);
        }
    }
}
