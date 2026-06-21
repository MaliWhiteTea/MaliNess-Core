package com.mertaliakcay.malinesscore.command;

import com.mertaliakcay.malinesscore.MaliNessCore;
import com.mertaliakcay.malinesscore.util.PluginLang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

final class MnCommandHelp {

    /** Toplam satir: 1 baslik + 18 komut + 1 sayfa navigasyonu = 20 */
    static final int COMMANDS_PER_PAGE = 18;

    private MnCommandHelp() {
    }

    static void send(MaliNessCore plugin, MalinessCommand commands, CommandSender sender, int requestedPage) {
        PluginLang lang = plugin.getPluginLang();
        List<HelpEntry> entries = collectEntries(commands, sender);

        if (entries.isEmpty()) {
            lang.send(sender, "mn-help-header");
            lang.send(sender, "mn-help-empty");
            return;
        }

        int totalPages = (int) Math.ceil(entries.size() / (double) COMMANDS_PER_PAGE);
        int page = Math.max(1, Math.min(requestedPage, totalPages));

        lang.send(sender, "mn-help-header");

        int start = (page - 1) * COMMANDS_PER_PAGE;
        int end = Math.min(start + COMMANDS_PER_PAGE, entries.size());
        for (HelpEntry entry : entries.subList(start, end)) {
            Component line = lang.get("mn-help-entry", "command", entry.label());
            line = line.clickEvent(ClickEvent.runCommand(entry.runCommand()))
                    .hoverEvent(HoverEvent.showText(lang.get(
                            "mn-help-hover",
                            "description", lang.getText("mn-help-desc-" + entry.id()),
                            "mn", entry.mnUsage(),
                            "direct", entry.directUsage()
                    )));
            sender.sendMessage(line);
        }

        sendNavigation(plugin, lang, sender, page, totalPages);
    }

    private static void sendNavigation(MaliNessCore plugin, PluginLang lang, CommandSender sender, int page, int totalPages) {
        Component navigation = plugin.getMessageService().prefix()
                .append(lang.getPlain("mn-help-nav-page", "current", page, "total", totalPages))
                .append(Component.space());

        if (page > 1) {
            navigation = navigation.append(
                    lang.getPlain("mn-help-nav-prev")
                            .clickEvent(ClickEvent.runCommand("/mn " + (page - 1)))
                            .hoverEvent(HoverEvent.showText(lang.getPlain("mn-help-nav-prev-hover")))
            );
        } else {
            navigation = navigation.append(lang.getPlain("mn-help-nav-prev-disabled"));
        }

        navigation = navigation.append(Component.space());

        if (page < totalPages) {
            navigation = navigation.append(
                    lang.getPlain("mn-help-nav-next")
                            .clickEvent(ClickEvent.runCommand("/mn " + (page + 1)))
                            .hoverEvent(HoverEvent.showText(lang.getPlain("mn-help-nav-next-hover")))
            );
        } else {
            navigation = navigation.append(lang.getPlain("mn-help-nav-next-disabled"));
        }

        sender.sendMessage(navigation);
    }

    private static List<HelpEntry> collectEntries(MalinessCommand commands, CommandSender sender) {
        List<HelpEntry> entries = new ArrayList<>();

        if (sender.hasPermission(MalinessCommand.PERM_RELOAD)) {
            entries.add(entry("reload", "/reload", "/mn reload", "/mn reload", "(yalnızca /mn)"));
        }

        if (commands.isHealAvailable(sender)) {
            entries.add(entry("heal", "/heal", "/mn heal", "/mn heal [oyuncu]", "/heal [oyuncu] | /iyilestir"));
        }
        if (commands.isFeedAvailable(sender)) {
            entries.add(entry("feed", "/feed", "/mn feed", "/mn feed [oyuncu]", "/feed [oyuncu] | /doyur"));
        }
        if (commands.isHealthAvailable(sender)) {
            entries.add(entry("health", "/health", "/mn health", "/mn health <oyuncu> <deger>", "/health <oyuncu> <deger> | /can"));
        }
        if (commands.isHungerAvailable(sender)) {
            entries.add(entry("hunger", "/hunger", "/mn hunger", "/mn hunger <oyuncu> <deger>", "/hunger <oyuncu> <deger> | /aclik"));
        }
        if (commands.isSaturateAvailable(sender)) {
            entries.add(entry("saturate", "/saturate", "/mn saturate", "/mn saturate [oyuncu]", "/saturate [oyuncu] | /doygunluk"));
        }
        if (commands.isSaturationAvailable(sender)) {
            entries.add(entry("saturation", "/saturation", "/mn saturation", "/mn saturation <oyuncu> <deger>", "/saturation <oyuncu> <deger>"));
        }
        if (commands.isGodAvailable(sender)) {
            entries.add(entry("god", "/god", "/mn god", "/mn god [oyuncu]", "/god [oyuncu] | /tanri"));
        }
        if (commands.isHomeSetAvailable(sender)) {
            entries.add(entry("sethome", "/sethome", "/mn sethome", "/mn sethome [ev]", "/sethome [ev] | /evayarla"));
        }
        if (commands.isHomeTeleportAvailable(sender)) {
            entries.add(entry("home", "/home", "/mn home", "/mn home [ev]", "/home [ev] | /ev"));
        }
        if (commands.isHomeDeleteAvailable(sender)) {
            entries.add(entry("delhome", "/delhome", "/mn delhome", "/mn delhome [ev]", "/delhome [ev] | /evsil"));
        }
        if (commands.isHomeListAvailable(sender)) {
            entries.add(entry("homes", "/homes", "/mn homes", "/mn homes [oyuncu]", "/homes [oyuncu]"));
        }
        if (commands.isHomeRenameAvailable(sender)) {
            entries.add(entry("renamehome", "/renamehome", "/mn renamehome", "/mn renamehome <eski> <yeni>", "/renamehome <eski> <yeni>"));
        }
        if (commands.isPlaytimeAvailable(sender)) {
            entries.add(entry("playtime", "/playtime", "/mn playtime", "/mn playtime [oyuncu]", "/playtime [oyuncu] | /oynamasüresi"));
        }
        if (commands.isBroadcastAvailable(sender)) {
            entries.add(entry("broadcast", "/broadcast", "/mn broadcast", "/mn broadcast <hedef> <mesaj>", "/broadcast | /bc | /duyur"));
        }
        if (commands.isVanishAvailable(sender)) {
            entries.add(entry("vanish", "/vanish", "/mn vanish", "/mn vanish [oyuncu|list]", "/vanish [oyuncu|list] | /gizlen"));
        }
        if (commands.isWarpAvailable(sender)) {
            entries.add(entry("warp", "/warp", "/mn warp", "/mn warp [warp|sayfa]", "/warp [warp|sayfa]"));
            entries.add(entry("warps", "/warps", "/mn warps", "/mn warps [sayfa]", "/warps [sayfa] | /warplar"));
        }
        if (commands.isSystemsListAvailable(sender)) {
            entries.add(entry("systems", "/systems", "/mn systems", "/mn systems [sayfa]", "/systems | /sistemler"));
        }
        if (commands.isSystemControlAvailable(sender)) {
            entries.add(entry("system", "/system", "/mn system", "/mn system <on|off|info> <sistem>", "/system | /sistem"));
        }

        return entries;
    }

    private static HelpEntry entry(String id, String label, String runCommand, String mnUsage, String directUsage) {
        return new HelpEntry(id, label, runCommand, mnUsage, directUsage);
    }

    private record HelpEntry(String id, String label, String runCommand, String mnUsage, String directUsage) {
    }
}
