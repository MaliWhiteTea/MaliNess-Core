package com.mertaliakcay.malinesscore.systems.economy.command;

import com.mertaliakcay.malinesscore.systems.economy.EconomyConstants;
import com.mertaliakcay.malinesscore.systems.economy.EconomySystem;
import com.mertaliakcay.malinesscore.systems.economy.EconomyFormatter;
import com.mertaliakcay.malinesscore.systems.economy.EconomyService;
import com.mertaliakcay.malinesscore.systems.economy.model.CurrencyDefinition;
import com.mertaliakcay.malinesscore.systems.economy.model.TransactionResult;
import com.mertaliakcay.malinesscore.util.CommandSuggestions;
import com.mertaliakcay.malinesscore.util.SystemLang;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class EconomyMnCommand {

    private static final Set<String> ACTIONS = Set.of("give", "take", "set", "reset", "info", "server");

    private final EconomyService economyService;
    private final SystemLang lang;

    public EconomyMnCommand(EconomyService economyService, SystemLang lang) {
        this.economyService = economyService;
        this.lang = lang;
    }

    public void handle(CommandSender sender, String[] args) {
        if (!economyService.isAvailable()) {
            lang.send(sender, "economy-unavailable");
            return;
        }

        if (!sender.hasPermission(EconomySystem.PERM_ADMIN)) {
            lang.send(sender, "no-permission");
            return;
        }

        if (args.length == 0) {
            lang.send(sender, "eco-usage");
            return;
        }

        String action = args[0].toLowerCase(Locale.ROOT);
        if ("server".equals(action)) {
            handleServerInfo(sender);
            return;
        }

        if (args.length < 2) {
            lang.send(sender, "eco-usage");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (target.getName() == null && !target.hasPlayedBefore()) {
            lang.send(sender, "player-not-found", "player", args[1]);
            return;
        }

        String currencyId = EconomyConstants.PRIMARY_CURRENCY;
        if (args.length >= 4) {
            currencyId = args[3].toLowerCase(Locale.ROOT);
        }

        CurrencyDefinition currency = economyService.getCurrencyRegistry().get(currencyId).orElse(null);
        if (currency == null) {
            lang.send(sender, "currency-not-found", "currency", currencyId);
            return;
        }

        switch (action) {
            case "info" -> {
                BigDecimal balance = economyService.getBalance(target, currencyId);
                lang.send(sender, "eco-info",
                        "player", target.getName(),
                        "balance", economyService.format(balance, currencyId),
                        "currency", currency.getDisplayName());
            }
            case "reset" -> {
                TransactionResult result = economyService.setBalance(
                        target.getUniqueId(),
                        currencyId,
                        currency.getDefaultBalance(),
                        "eco-reset"
                );
                sendResult(sender, result, "eco-reset-success", target.getName(), currency);
            }
            case "give", "take", "set" -> {
                if (args.length < 3) {
                    lang.send(sender, "eco-usage");
                    return;
                }
                BigDecimal amount = EconomyFormatter.parseAmount(args[2], currency.getDecimalPlaces());
                if (EconomyFormatter.isZeroOrNegative(amount) && !"set".equals(action)) {
                    lang.send(sender, "invalid-amount");
                    return;
                }
                if (amount == null) {
                    lang.send(sender, "invalid-amount");
                    return;
                }

                TransactionResult result = switch (action) {
                    case "give" -> economyService.deposit(target.getUniqueId(), currencyId, amount, "admin-give", "eco");
                    case "take" -> economyService.withdraw(target.getUniqueId(), currencyId, amount, "admin-take", "eco");
                    case "set" -> economyService.setBalance(target.getUniqueId(), currencyId, amount, "eco-set");
                    default -> TransactionResult.INTERNAL_ERROR;
                };
                sendResult(sender, result, "eco-success", target.getName(), currency);
            }
            default -> lang.send(sender, "eco-usage");
        }
    }

    private void handleServerInfo(CommandSender sender) {
        CurrencyDefinition currency = economyService.getCurrencyRegistry().getPrimary();
        BigDecimal balance = economyService.getBalance(EconomyConstants.SERVER_ACCOUNT_ID, currency.getId());
        lang.send(sender, "eco-server-info",
                "balance", economyService.format(balance, currency.getId()));
    }

    private void sendResult(CommandSender sender, TransactionResult result, String successKey, String player, CurrencyDefinition currency) {
        if (result == TransactionResult.SUCCESS) {
            BigDecimal balance = economyService.getBalance(Bukkit.getOfflinePlayer(player), currency.getId());
            lang.send(sender, successKey,
                    "player", player,
                    "balance", economyService.format(balance, currency.getId()));
            return;
        }
        lang.send(sender, "eco-failed", "reason", result.name());
    }

    public static boolean isEcoSubcommand(String arg) {
        return "eco".equalsIgnoreCase(arg);
    }

    public List<String> suggest(CommandSender sender, String[] args) {
        if (!sender.hasPermission(EconomySystem.PERM_ADMIN)) {
            return List.of();
        }
        if (args.length == 0) {
            return filter(ACTIONS, "");
        }
        if (args.length == 1) {
            if (isPlayerAction(args[0]) && ACTIONS.contains(args[0].toLowerCase(Locale.ROOT))) {
                return CommandSuggestions.onlinePlayerNames(sender, "", false);
            }
            return filter(ACTIONS, args[0]);
        }
        if (args.length == 2 && isPlayerAction(args[0])) {
            return CommandSuggestions.onlinePlayerNames(sender, args[1], false);
        }
        if (args.length == 3 && Set.of("give", "take", "set").contains(args[0].toLowerCase(Locale.ROOT))) {
            return List.of("100", "500", "1000");
        }
        if (args.length == 4) {
            return filter(economyService.getCurrencyRegistry().getAll().stream().map(CurrencyDefinition::getId).toList(), args[3]);
        }
        return List.of();
    }

    private static boolean isPlayerAction(String action) {
        if (action == null) {
            return false;
        }
        return Set.of("give", "take", "set", "reset", "info").contains(action.toLowerCase(Locale.ROOT));
    }

    private List<String> filter(Iterable<String> values, String prefix) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<>();
        for (String value : values) {
            if (value.toLowerCase(Locale.ROOT).startsWith(lower)) {
                result.add(value);
            }
        }
        return result;
    }
}
