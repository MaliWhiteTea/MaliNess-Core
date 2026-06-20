package com.mertaliakcay.malinesscore.systems.broadcast;

import com.mertaliakcay.malinesscore.MaliNessCore;
import com.mertaliakcay.malinesscore.util.MaliNessColorUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class BroadcastCommand {

    private final BroadcastSystem system;
    private final MaliNessCore plugin;
    private Set<String> allAliases = Set.of("hepsi", "all", "everyone");
    private int cooldownSeconds = 1;

    public BroadcastCommand(BroadcastSystem system) {
        this.system = system;
        this.plugin = system.getPlugin();
    }

    public void reloadFromConfig() {
        FileConfiguration configuration = system.getConfig().get();
        cooldownSeconds = Math.max(0, configuration.getInt("cooldown-seconds", 1));
        List<String> aliases = configuration.getStringList("targets.all-aliases");
        if (!aliases.isEmpty()) {
            allAliases = new HashSet<>();
            for (String alias : aliases) {
                allAliases.add(alias.toLowerCase(Locale.ROOT));
            }
        }
    }

    public void handle(CommandSender sender, String[] args) {
        if (!system.isEnabled()) {
            system.getLang().send(sender, "system-disabled");
            return;
        }

        if (!sender.hasPermission(BroadcastSystem.PERM_USE)) {
            system.getLang().send(sender, "no-permission");
            return;
        }

        if (args.length < 2) {
            system.getLang().send(sender, "usage");
            return;
        }

        if (sender instanceof Player player && cooldownSeconds > 0 && !checkCooldown(player)) {
            system.getLang().send(sender, "cooldown", "seconds", cooldownSeconds);
            return;
        }

        String targetArg = args[0].toLowerCase(Locale.ROOT);
        String message = parseMessage(args);
        if (message.isBlank()) {
            system.getLang().send(sender, "usage");
            return;
        }

        List<Player> recipients = resolveRecipients(targetArg);
        if (recipients == null) {
            system.getLang().send(sender, "world-not-found", "world", args[0]);
            return;
        }
        if (recipients.isEmpty()) {
            system.getLang().send(sender, "no-recipients");
            return;
        }

        Component payload = plugin.getMessageService().prefix()
                .append(MaliNessColorUtil.toComponent(message, sender, plugin));

        for (Player recipient : recipients) {
            recipient.sendMessage(payload);
        }

        if (sender instanceof Player player && cooldownSeconds > 0) {
            markCooldown(player);
        }

        system.getLang().send(sender, "sent", "count", recipients.size());
    }

    public List<String> suggest(CommandSender sender, String[] args) {
        if (!system.isEnabled() || !sender.hasPermission(BroadcastSystem.PERM_USE)) {
            return List.of();
        }

        if (args.length <= 1) {
            List<String> suggestions = new ArrayList<>(allAliases);
            for (World world : Bukkit.getWorlds()) {
                suggestions.add(world.getName());
            }
            return com.mertaliakcay.malinesscore.util.CommandSuggestions.filter(
                    suggestions,
                    args.length == 0 ? "" : args[0]
            );
        }

        return List.of();
    }

    public boolean canSuggest(CommandSender sender) {
        return system.isEnabled() && sender.hasPermission(BroadcastSystem.PERM_USE);
    }

    private List<Player> resolveRecipients(String targetArg) {
        if (allAliases.contains(targetArg)) {
            return new ArrayList<>(Bukkit.getOnlinePlayers());
        }

        World world = Bukkit.getWorld(targetArg);
        if (world == null) {
            for (World candidate : Bukkit.getWorlds()) {
                if (candidate.getName().equalsIgnoreCase(targetArg)) {
                    world = candidate;
                    break;
                }
            }
        }

        if (world == null) {
            return null;
        }

        return new ArrayList<>(world.getPlayers());
    }

    private String parseMessage(String[] args) {
        String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)).trim();
        if (message.length() >= 2 && message.startsWith("\"") && message.endsWith("\"")) {
            message = message.substring(1, message.length() - 1);
        }
        return message;
    }

    private boolean checkCooldown(Player player) {
        Long lastUsed = system.getCooldowns().get(player.getUniqueId());
        if (lastUsed == null) {
            return true;
        }
        return System.currentTimeMillis() - lastUsed >= cooldownSeconds * 1000L;
    }

    private void markCooldown(Player player) {
        system.getCooldowns().put(player.getUniqueId(), System.currentTimeMillis());
    }
}
