package com.mertaliakcay.malinesscore.systems.pwarp;

import com.mertaliakcay.malinesscore.MaliNessCore;
import com.mertaliakcay.malinesscore.systems.pwarp.model.Pwarp;
import com.mertaliakcay.malinesscore.util.ColorUtil;
import com.mertaliakcay.malinesscore.util.SystemLang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.CommandSender;

import java.util.Comparator;
import java.util.List;

final class PwarpListHelp {

    static final int PWARPS_PER_PAGE = 8;

    private PwarpListHelp() {
    }

    static void send(
            MaliNessCore plugin,
            PwarpService service,
            CommandSender sender,
            int requestedPage
    ) {
        SystemLang lang = service.getLang();
        List<Pwarp> entries = service.getAllPwarps().stream()
                .sorted(Comparator.comparing(Pwarp::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        if (entries.isEmpty()) {
            lang.send(sender, "list-empty");
            return;
        }

        int totalPages = Math.max(1, (int) Math.ceil(entries.size() / (double) PWARPS_PER_PAGE));
        int page = Math.max(1, Math.min(requestedPage, totalPages));

        lang.send(sender, "list-header", "count", entries.size());

        int start = (page - 1) * PWARPS_PER_PAGE;
        int end = Math.min(start + PWARPS_PER_PAGE, entries.size());
        for (Pwarp pwarp : entries.subList(start, end)) {
            sender.sendMessage(buildRow(plugin, lang, service, pwarp));
        }

        if (totalPages > 1) {
            sendNavigation(plugin, lang, sender, page, totalPages);
        }
    }

    private static Component buildRow(
            MaliNessCore plugin,
            SystemLang lang,
            PwarpService service,
            Pwarp pwarp
    ) {
        Component hover = buildHover(lang, service, pwarp);

        return plugin.getMessageService().prefix()
                .append(lang.getPlain("list-entry-prefix"))
                .append(lang.getPlain("list-entry-name", "pwarp", pwarp.getName()))
                .clickEvent(ClickEvent.runCommand("/pwarp " + pwarp.getName()))
                .hoverEvent(HoverEvent.showText(hover));
    }

    private static Component buildHover(SystemLang lang, PwarpService service, Pwarp pwarp) {
        Component hover = lang.getPlain(
                "list-hover",
                "pwarp", pwarp.getName(),
                "owner", pwarp.getOwnerName(),
                "world", pwarp.getWorldName(),
                "x", (int) pwarp.getX(),
                "y", (int) pwarp.getY(),
                "z", (int) pwarp.getZ(),
                "created", service.formatCreatedAt(pwarp),
                "visits", pwarp.getVisitCount(),
                "last-visit", service.formatLastVisit(pwarp)
        );

        String description = pwarp.getDescription();
        if (description != null && !description.isBlank()) {
            hover = hover.append(Component.newline()).append(ColorUtil.colorize(description));
        }

        return hover;
    }

    private static void sendNavigation(
            MaliNessCore plugin,
            SystemLang lang,
            CommandSender sender,
            int page,
            int totalPages
    ) {
        Component navigation = plugin.getMessageService().prefix()
                .append(lang.getPlain("list-nav-page", "current", page, "total", totalPages))
                .append(Component.space());

        if (page > 1) {
            navigation = navigation.append(
                    lang.getPlain("list-nav-prev")
                            .clickEvent(ClickEvent.runCommand("/pwarp " + (page - 1)))
                            .hoverEvent(HoverEvent.showText(lang.getPlain("list-nav-prev-hover")))
            );
        } else {
            navigation = navigation.append(lang.getPlain("list-nav-prev-disabled"));
        }

        navigation = navigation.append(Component.space());

        if (page < totalPages) {
            navigation = navigation.append(
                    lang.getPlain("list-nav-next")
                            .clickEvent(ClickEvent.runCommand("/pwarp " + (page + 1)))
                            .hoverEvent(HoverEvent.showText(lang.getPlain("list-nav-next-hover")))
            );
        } else {
            navigation = navigation.append(lang.getPlain("list-nav-next-disabled"));
        }

        sender.sendMessage(navigation);
    }
}
