package com.mertaliakcay.malinesscore.gui;

import com.mertaliakcay.malinesscore.gui.content.MenuContentProvider;
import com.mertaliakcay.malinesscore.gui.model.MenuClickType;
import com.mertaliakcay.malinesscore.gui.model.MenuItemDefinition;
import com.mertaliakcay.malinesscore.gui.model.MenuSession;
import com.mertaliakcay.malinesscore.util.SystemLang;
import org.bukkit.entity.Player;

public final class MenuActionExecutor {

    private final MenuService menuService;
    private final MenuRenderer renderer;
    private final MenuRegistry registry;
    private final SystemLang lang;

    public MenuActionExecutor(MenuService menuService, MenuRenderer renderer, MenuRegistry registry, SystemLang lang) {
        this.menuService = menuService;
        this.renderer = renderer;
        this.registry = registry;
        this.lang = lang;
    }

    public void execute(Player player, MenuSession session, String action, MenuClickType clickType) {
        if (action == null || action.isBlank() || action.equalsIgnoreCase("noop")) {
            return;
        }

        String normalized = action.toLowerCase();
        if (normalized.startsWith("open-menu:")) {
            String targetId = action.substring("open-menu:".length()).trim();
            menuService.open(player, targetId);
            return;
        }

        switch (normalized) {
            case "close" -> menuService.close(player, true);
            case "refresh" -> menuService.refreshView(player, session);
            case "page-prev" -> changePage(player, session, session.getCurrentPage() - 1);
            case "page-next" -> changePage(player, session, session.getCurrentPage() + 1);
            case "sort-cycle" -> {
                session.cycleSortMode();
                refreshContent(session, player);
            }
            case "filter-all" -> {
                session.setFilterMode(MenuSession.FilterMode.ALL);
                refreshContent(session, player);
            }
            case "filter-favorites" -> {
                session.setFilterMode(MenuSession.FilterMode.FAVORITES);
                refreshContent(session, player);
            }
            case "filter-mine" -> {
                session.setFilterMode(MenuSession.FilterMode.MINE);
                refreshContent(session, player);
            }
            case "demo-click" -> player.sendMessage(lang.getPlain(
                    "demo-click",
                    player,
                    "click", clickType.toYamlKey()
            ));
            default -> {
                if (normalized.startsWith("demo-click-")) {
                    player.sendMessage(lang.getPlain(
                            "demo-click",
                            player,
                            "click", clickType.toYamlKey()
                    ));
                }
            }
        }
    }

    public void executeChromeClick(Player player, MenuSession session, MenuItemDefinition item, MenuClickType clickType) {
        String action = item.getClicks().get(clickType);
        if (action == null) {
            return;
        }
        execute(player, session, action, clickType);
    }

    public void executeContentClick(Player player, MenuSession session, String entryId, MenuClickType clickType) {
        MenuContentProvider provider = registry.getContentProvider(session.getDefinition().getContentProviderId());
        if (provider != null) {
            provider.onContentClick(player, session, entryId, clickType);
        }
    }

    private void changePage(Player player, MenuSession session, int targetPage) {
        int page = Math.clamp(targetPage, 1, session.getMaxPage());
        if (page == session.getCurrentPage()) {
            return;
        }
        session.setCurrentPage(page);
        refreshContent(session, player);
    }

    private void refreshContent(MenuSession session, Player player) {
        MenuContentProvider provider = registry.getContentProvider(session.getDefinition().getContentProviderId());
        if (provider != null) {
            session.setMaxPage(provider.getTotalPages(session));
        }
        menuService.refreshView(player, session);
    }
}
