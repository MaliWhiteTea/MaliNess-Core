package com.mertaliakcay.malinesscore.systems.warp;

import com.mertaliakcay.malinesscore.MaliNessCore;
import com.mertaliakcay.malinesscore.systems.warp.model.Warp;
import com.mertaliakcay.malinesscore.util.ColorUtil;
import com.mertaliakcay.malinesscore.util.SystemLang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.CommandSender;

import java.util.Comparator;
import java.util.List;

final class WarpListHelp {

    static final int WARPS_PER_PAGE = 8;

    private WarpListHelp() {
    }

    static void send(MaliNessCore plugin, WarpService service, CommandSender sender, int requestedPage) {
        SystemLang lang = service.getLang();
        List<Warp> entries = service.getVisibleWarps(sender).stream()
                .sorted(Comparator.comparing(warp -> warp.getName(), String.CASE_INSENSITIVE_ORDER))
                .toList();

        if (entries.isEmpty()) {
            lang.send(sender, "list-empty");
            return;
        }

        int totalPages = Math.max(1, (int) Math.ceil(entries.size() / (double) WARPS_PER_PAGE));
        int page = Math.max(1, Math.min(requestedPage, totalPages));

        lang.send(sender, "list-header", "count", entries.size());

        int start = (page - 1) * WARPS_PER_PAGE;
        int end = Math.min(start + WARPS_PER_PAGE, entries.size());
        for (Warp warp : entries.subList(start, end)) {
            sender.sendMessage(buildRow(plugin, lang, sender, service, warp));
        }

        if (totalPages > 1) {
            sendNavigation(plugin, lang, sender, page, totalPages);
        }
    }

    private static Component buildRow(
            MaliNessCore plugin,
            SystemLang lang,
            CommandSender sender,
            WarpService service,
            Warp warp
    ) {
        Component row = plugin.getMessageService().prefix()
                .append(lang.getPlain("list-entry-prefix"))
                .append(lang.getPlain(warp.isEnabled() ? "list-entry-open" : "list-entry-closed", "warp", warp.getName()));

        Component hover = buildHover(lang, warp);
        row = row.hoverEvent(HoverEvent.showText(hover));

        if (warp.isEnabled()) {
            row = row.clickEvent(ClickEvent.runCommand("/warp " + warp.getName()))
                    .hoverEvent(HoverEvent.showText(hover));
        } else if (service.canSeeClosed(sender)) {
            row = row.clickEvent(ClickEvent.runCommand("/warp " + warp.getName()))
                    .hoverEvent(HoverEvent.showText(hover));
        }

        return row;
    }

    private static Component buildHover(SystemLang lang, Warp warp) {
        String description = warp.getDescription();
        if (description == null || description.isBlank()) {
            if (warp.isEnabled()) {
                return lang.getPlain("list-hover-name-only", "warp", warp.getName());
            }
            return lang.getPlain("list-hover-closed", "warp", warp.getName());
        }

        Component descriptionComponent = ColorUtil.colorize(description);
        if (warp.isEnabled()) {
            return lang.getPlain("list-hover-name-only", "warp", warp.getName())
                    .append(Component.newline())
                    .append(descriptionComponent);
        }

        return lang.getPlain("list-hover-closed", "warp", warp.getName())
                .append(Component.newline())
                .append(descriptionComponent);
    }

    private static void sendNavigation(MaliNessCore plugin, SystemLang lang, CommandSender sender, int page, int totalPages) {
        Component navigation = plugin.getMessageService().prefix()
                .append(lang.getPlain("list-nav-page", "current", page, "total", totalPages))
                .append(Component.space());

        if (page > 1) {
            navigation = navigation.append(
                    lang.getPlain("list-nav-prev")
                            .clickEvent(ClickEvent.runCommand("/warp " + (page - 1)))
                            .hoverEvent(HoverEvent.showText(lang.getPlain("list-nav-prev-hover")))
            );
        } else {
            navigation = navigation.append(lang.getPlain("list-nav-prev-disabled"));
        }

        navigation = navigation.append(Component.space());

        if (page < totalPages) {
            navigation = navigation.append(
                    lang.getPlain("list-nav-next")
                            .clickEvent(ClickEvent.runCommand("/warp " + (page + 1)))
                            .hoverEvent(HoverEvent.showText(lang.getPlain("list-nav-next-hover")))
            );
        } else {
            navigation = navigation.append(lang.getPlain("list-nav-next-disabled"));
        }

        sender.sendMessage(navigation);
    }
}
