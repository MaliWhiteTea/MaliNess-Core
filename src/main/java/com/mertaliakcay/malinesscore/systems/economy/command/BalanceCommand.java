package com.mertaliakcay.malinesscore.systems.economy.command;

import com.mertaliakcay.malinesscore.systems.economy.EconomyConstants;
import com.mertaliakcay.malinesscore.systems.economy.EconomySystem;
import com.mertaliakcay.malinesscore.systems.economy.EconomyService;
import com.mertaliakcay.malinesscore.systems.economy.model.CurrencyDefinition;
import com.mertaliakcay.malinesscore.util.CommandSuggestions;
import com.mertaliakcay.malinesscore.util.SystemLang;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

public final class BalanceCommand {

    private final EconomyService economyService;
    private final SystemLang lang;

    public BalanceCommand(EconomyService economyService, SystemLang lang) {
        this.economyService = economyService;
        this.lang = lang;
    }

    public void handle(CommandSender sender, String[] args) {
        if (!economyService.isAvailable()) {
            lang.send(sender, "economy-unavailable");
            return;
        }

        OfflinePlayer target;
        String currencyId = EconomyConstants.PRIMARY_CURRENCY;

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                lang.send(sender, "player-only");
                return;
            }
            if (!sender.hasPermission(EconomySystem.PERM_BALANCE)) {
                lang.send(sender, "no-permission");
                return;
            }
            target = player;
        } else if (args.length == 1) {
            if (sender instanceof Player && sender.hasPermission(EconomySystem.PERM_BALANCE_OTHERS)) {
                target = Bukkit.getOfflinePlayer(args[0]);
                if (target.getName() == null && !target.hasPlayedBefore()) {
                    lang.send(sender, "player-not-found", "player", args[0]);
                    return;
                }
            } else if (economyService.getCurrencyRegistry().exists(args[0])) {
                if (!(sender instanceof Player player)) {
                    lang.send(sender, "player-only");
                    return;
                }
                target = player;
                currencyId = args[0].toLowerCase(Locale.ROOT);
            } else {
                if (!sender.hasPermission(EconomySystem.PERM_BALANCE_OTHERS)) {
                    lang.send(sender, "no-permission");
                    return;
                }
                target = Bukkit.getOfflinePlayer(args[0]);
                if (target.getName() == null && !target.hasPlayedBefore()) {
                    lang.send(sender, "player-not-found", "player", args[0]);
                    return;
                }
            }
        } else {
            if (!sender.hasPermission(EconomySystem.PERM_BALANCE_OTHERS)) {
                lang.send(sender, "no-permission");
                return;
            }
            target = Bukkit.getOfflinePlayer(args[0]);
            currencyId = args[1].toLowerCase(Locale.ROOT);
            if (target.getName() == null && !target.hasPlayedBefore()) {
                lang.send(sender, "player-not-found", "player", args[0]);
                return;
            }
        }

        CurrencyDefinition currency = economyService.getCurrencyRegistry().get(currencyId).orElse(null);
        if (currency == null) {
            lang.send(sender, "currency-not-found", "currency", currencyId);
            return;
        }

        BigDecimal balance = economyService.getBalance(target, currencyId);
        String targetName = target.getName() != null ? target.getName() : target.getUniqueId().toString();
        if (sender instanceof Player player && player.getUniqueId().equals(target.getUniqueId())) {
            lang.send(sender, "balance-self",
                    "balance", economyService.format(balance, currencyId));
        } else {
            lang.send(sender, "balance-other",
                    "player", targetName,
                    "balance", economyService.format(balance, currencyId));
        }
    }

    public List<String> suggest(CommandSender sender, String[] args) {
        if (!sender.hasPermission(EconomySystem.PERM_BALANCE_OTHERS)) {
            return List.of();
        }
        if (args.length == 0) {
            return CommandSuggestions.onlinePlayerNames(sender, "", false);
        }
        if (args.length == 1) {
            return CommandSuggestions.onlinePlayerNames(sender, args[0], false);
        }
        return List.of();
    }
}
