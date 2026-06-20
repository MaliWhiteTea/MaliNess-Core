package com.mertaliakcay.malinesscore.systems.playtime;

import com.mertaliakcay.malinesscore.util.CommandSuggestions;
import com.mertaliakcay.malinesscore.systems.vanish.VanishService;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PlaytimeCommand {

    private final PlaytimeSystem system;

    public PlaytimeCommand(PlaytimeSystem system) {
        this.system = system;
    }

    public void handle(CommandSender sender, String[] args) {
        if (!system.isEnabled()) {
            system.getLang().send(sender, "system-disabled");
            return;
        }

        if (args.length == 0) {
            handleSelf(sender);
            return;
        }

        if (args.length == 1) {
            handleOther(sender, args[0]);
            return;
        }

        system.getLang().send(sender, "usage");
    }

    public List<String> suggest(CommandSender sender, String[] args) {
        if (!system.isEnabled() || !canSuggest(sender)) {
            return Collections.emptyList();
        }

        if (args.length <= 1 && sender.hasPermission(PlaytimeSystem.PERM_OTHERS)) {
            return CommandSuggestions.filter(onlinePlayerNames(sender), args.length == 0 ? "" : args[0]);
        }

        return Collections.emptyList();
    }

    public boolean canSuggest(CommandSender sender) {
        return system.isEnabled()
                && (sender.hasPermission(PlaytimeSystem.PERM_USE)
                || sender.hasPermission(PlaytimeSystem.PERM_OTHERS));
    }

    private void handleSelf(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            system.getLang().send(sender, "console-no-player");
            return;
        }

        if (!sender.hasPermission(PlaytimeSystem.PERM_USE)) {
            system.getLang().send(sender, "no-permission");
            return;
        }

        system.getLang().send(sender, "self", "time", system.getPlaytimeService().getFormatted(player.getUniqueId()));
    }

    private void handleOther(CommandSender sender, String targetName) {
        if (!sender.hasPermission(PlaytimeSystem.PERM_OTHERS)) {
            system.getLang().send(sender, "no-permission-others");
            return;
        }

        OfflinePlayer target = resolveTarget(targetName);
        if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {
            system.getLang().send(sender, "player-not-found", "player", targetName);
            return;
        }

        String displayName = target.getName() != null ? target.getName() : targetName;
        system.getLang().send(sender, "other",
                "player", displayName,
                "time", system.getPlaytimeService().getFormatted(target));
    }

    private OfflinePlayer resolveTarget(String targetName) {
        Player online = Bukkit.getPlayerExact(targetName);
        if (online != null) {
            return online;
        }
        return Bukkit.getOfflinePlayer(targetName);
    }

    private List<String> onlinePlayerNames(CommandSender sender) {
        List<String> names = Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .toList();

        VanishService vanishService = system.getPlugin().getVanishService();
        if (vanishService != null) {
            return vanishService.filterPlayerNames(sender, names);
        }

        return new ArrayList<>(names);
    }
}
