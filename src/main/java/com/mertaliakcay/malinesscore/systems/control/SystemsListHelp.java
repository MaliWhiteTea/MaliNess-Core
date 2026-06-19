package com.mertaliakcay.malinesscore.systems.control;

import com.mertaliakcay.malinesscore.MaliNessCore;
import com.mertaliakcay.malinesscore.util.PluginLang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.CommandSender;

import java.util.List;

final class SystemsListHelp {

    /** Toplam satir: 1 baslik + 18 sistem + 1 sayfa navigasyonu = 20 */
    static final int SYSTEMS_PER_PAGE = 18;

    private SystemsListHelp() {
    }

    static int pageForSystem(SystemControlService control, String systemId) {
        List<SystemDescriptor> entries = control.getCatalog().listAll();
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getId().equalsIgnoreCase(systemId)) {
                return (i / SYSTEMS_PER_PAGE) + 1;
            }
        }
        return 1;
    }

    static void send(MaliNessCore plugin, SystemControlService control, CommandSender sender, int requestedPage) {
        PluginLang lang = plugin.getPluginLang();
        List<SystemDescriptor> entries = control.getCatalog().listAll();

        int totalPages = Math.max(1, (int) Math.ceil(entries.size() / (double) SYSTEMS_PER_PAGE));
        int page = Math.max(1, Math.min(requestedPage, totalPages));

        lang.send(sender, "systems-list-header");

        int start = (page - 1) * SYSTEMS_PER_PAGE;
        int end = Math.min(start + SYSTEMS_PER_PAGE, entries.size());
        for (SystemDescriptor descriptor : entries.subList(start, end)) {
            sender.sendMessage(buildRow(plugin, control, lang, sender, descriptor));
        }

        sendNavigation(plugin, lang, sender, page, totalPages);
    }

    private static Component buildRow(
            MaliNessCore plugin,
            SystemControlService control,
            PluginLang lang,
            CommandSender sender,
            SystemDescriptor descriptor
    ) {
        String systemId = descriptor.getId();
        boolean active = control.isActive(descriptor);
        String statusKey = active ? "systems-list-status-active" : "systems-list-status-inactive";

        Component row = plugin.getMessageService().prefix()
                .append(lang.getPlain("systems-list-entry-prefix"))
                .append(lang.getPlain(statusKey))
                .append(Component.space())
                .append(lang.getPlain("systems-list-entry-name", "id", systemId))
                .hoverEvent(HoverEvent.showText(lang.get(
                        "systems-list-hover",
                        "description", lang.getText("systems-desc-" + systemId)
                )));

        if (!descriptor.isVirtual() && descriptor.isClosable()) {
            boolean canManage = control.canManage(sender, systemId);
            if (canManage) {
                if (active) {
                    row = row.append(Component.space()).append(
                            lang.getPlain("systems-list-action-off")
                                    .clickEvent(ClickEvent.runCommand("/system off " + systemId))
                                    .hoverEvent(HoverEvent.showText(lang.getPlain("systems-list-action-off-hover")))
                    );
                } else {
                    row = row.append(Component.space()).append(
                            lang.getPlain("systems-list-action-on")
                                    .clickEvent(ClickEvent.runCommand("/system on " + systemId))
                                    .hoverEvent(HoverEvent.showText(lang.getPlain("systems-list-action-on-hover")))
                    );
                }
            } else {
                row = row.append(Component.space()).append(
                        lang.getPlain("systems-list-no-permission-marker")
                                .hoverEvent(HoverEvent.showText(lang.getPlain("systems-list-no-permission-hover")))
                );
            }
        }

        return row;
    }

    private static void sendNavigation(MaliNessCore plugin, PluginLang lang, CommandSender sender, int page, int totalPages) {
        Component navigation = plugin.getMessageService().prefix()
                .append(lang.getPlain("systems-list-nav-page", "current", page, "total", totalPages))
                .append(Component.space());

        if (page > 1) {
            navigation = navigation.append(
                    lang.getPlain("systems-list-nav-prev")
                            .clickEvent(ClickEvent.runCommand("/systems " + (page - 1)))
                            .hoverEvent(HoverEvent.showText(lang.getPlain("systems-list-nav-prev-hover")))
            );
        } else {
            navigation = navigation.append(lang.getPlain("systems-list-nav-prev-disabled"));
        }

        navigation = navigation.append(Component.space());

        if (page < totalPages) {
            navigation = navigation.append(
                    lang.getPlain("systems-list-nav-next")
                            .clickEvent(ClickEvent.runCommand("/systems " + (page + 1)))
                            .hoverEvent(HoverEvent.showText(lang.getPlain("systems-list-nav-next-hover")))
            );
        } else {
            navigation = navigation.append(lang.getPlain("systems-list-nav-next-disabled"));
        }

        sender.sendMessage(navigation);
    }
}
