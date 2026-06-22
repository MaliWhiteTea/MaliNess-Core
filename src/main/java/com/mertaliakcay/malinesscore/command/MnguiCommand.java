package com.mertaliakcay.malinesscore.command;

import com.mertaliakcay.malinesscore.gui.MenuService;
import com.mertaliakcay.malinesscore.gui.model.ReleaseSource;
import com.mertaliakcay.malinesscore.systems.gui.GuiSystem;
import com.mertaliakcay.malinesscore.util.SystemLang;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MnguiCommand {

    private final MenuService menuService;
    private final SystemLang lang;

    public MnguiCommand(MenuService menuService, SystemLang lang) {
        this.menuService = menuService;
        this.lang = lang;
    }

    public void handle(CommandSender sender, String[] args) {
        if (args.length == 0) {
            lang.send(sender, "usage");
            return;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        if ("release".equals(sub)) {
            handleRelease(sender, args);
            return;
        }

        if (!(sender instanceof Player player)) {
            lang.send(sender, "player-only");
            return;
        }

        if (!canUse(player)) {
            lang.send(player, "no-permission");
            return;
        }

        switch (sub) {
            case "list" -> player.sendMessage(String.join(", ", menuService.getRegistry().getMenuIds()));
            case "open" -> {
                if (args.length < 2) {
                    lang.send(player, "usage");
                    return;
                }
                menuService.open(player, args[1]);
            }
            case "pwarp" -> menuService.open(player, "demo-pwarp-layout");
            case "pwarp-empty" -> menuService.open(player, "demo-pwarp-empty");
            case "mandatory" -> menuService.open(player, "demo-mandatory-afk");
            case "locked" -> menuService.open(player, "demo-overlay-locked");
            case "allowed" -> menuService.open(player, "demo-overlay-allowed");
            case "hopper" -> menuService.open(player, "demo-hopper");
            case "dropper" -> menuService.open(player, "demo-dropper");
            case "click-test" -> menuService.open(player, "demo-click-test");
            case "reload-test" -> {
                menuService.open(player, "demo-mandatory-afk");
                lang.send(player, "reload-test-hint");
            }
            default -> lang.send(player, "usage");
        }
    }

    private void handleRelease(CommandSender sender, String[] args) {
        if (!canRelease(sender)) {
            lang.send(sender, "no-permission");
            return;
        }

        Player target;
        if (args.length >= 2) {
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                lang.send(sender, "player-not-found", "player", args[1]);
                return;
            }
        } else if (sender instanceof Player player) {
            target = player;
        } else {
            lang.send(sender, "usage-release");
            return;
        }

        String detail = sender instanceof Player player
                ? "mngui-release"
                : "console-release";
        if (menuService.release(target, ReleaseSource.ADMIN, detail)) {
            if (sender == target) {
                lang.send(sender, "mandatory-released");
            } else {
                lang.send(sender, "mandatory-released-other", "player", target.getName());
                lang.send(target, "mandatory-released");
            }
            return;
        }

        if (sender == target) {
            lang.send(sender, "mandatory-not-active");
        } else {
            lang.send(sender, "mandatory-not-active-other", "player", target.getName());
        }
    }

    public List<String> suggest(CommandSender sender, String[] args) {
        if (!canUse(sender) && !canRelease(sender)) {
            return List.of();
        }

        if (args.length == 1) {
            List<String> options = new ArrayList<>(List.of(
                    "list", "open", "pwarp", "pwarp-empty", "mandatory", "release",
                    "locked", "allowed", "hopper", "dropper", "click-test", "reload-test"
            ));
            String prefix = args[0].toLowerCase(Locale.ROOT);
            List<String> result = new ArrayList<>();
            for (String option : options) {
                if (option.startsWith(prefix)) {
                    result.add(option);
                }
            }
            return result;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("release") && canRelease(sender)) {
            String prefix = args[1].toLowerCase(Locale.ROOT);
            List<String> result = new ArrayList<>();
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                    result.add(online.getName());
                }
            }
            return result;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("open") && sender instanceof Player) {
            String prefix = args[1].toLowerCase(Locale.ROOT);
            List<String> result = new ArrayList<>();
            for (String id : menuService.getRegistry().getMenuIds()) {
                if (id.startsWith(prefix)) {
                    result.add(id);
                }
            }
            return result;
        }

        return List.of();
    }

    private boolean canUse(CommandSender sender) {
        return sender.hasPermission(GuiSystem.PERM_MNGUI)
                || (sender instanceof Player player && player.isOp());
    }

    private boolean canRelease(CommandSender sender) {
        return sender.hasPermission(GuiSystem.PERM_MNGUI)
                || sender.hasPermission("maliness-core.mngui.release")
                || (sender instanceof Player player && player.isOp())
                || !(sender instanceof Player);
    }
}
