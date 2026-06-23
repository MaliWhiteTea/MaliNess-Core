package com.mertaliakcay.malinesscore.systems.gui;

import com.mertaliakcay.malinesscore.command.MnguiBasicCommand;
import com.mertaliakcay.malinesscore.command.MnguiCommand;
import com.mertaliakcay.malinesscore.gui.MandatorySessionStore;
import com.mertaliakcay.malinesscore.gui.MenuActionExecutor;
import com.mertaliakcay.malinesscore.gui.MenuListener;
import com.mertaliakcay.malinesscore.gui.MenuRegistry;
import com.mertaliakcay.malinesscore.gui.MenuRenderer;
import com.mertaliakcay.malinesscore.gui.MenuService;
import com.mertaliakcay.malinesscore.gui.content.DemoMandatoryAfkProvider;
import com.mertaliakcay.malinesscore.gui.content.DemoPwarpContentProvider;
import com.mertaliakcay.malinesscore.systems.AbstractGameSystem;

import java.util.List;

public final class GuiSystem extends AbstractGameSystem {

    public static final String PERM_MNGUI = "maliness-core.mngui";

    private MenuRegistry menuRegistry;
    private MenuService menuService;
    private MenuActionExecutor actionExecutor;
    private MnguiCommand mnguiCommand;

    @Override
    protected String getSystemId() {
        return "gui";
    }

    @Override
    protected void onRegister() {
        if (menuRegistry == null) {
            menuRegistry = new MenuRegistry(plugin);
            MandatorySessionStore mandatorySessionStore = new MandatorySessionStore();
            MenuRenderer renderer = new MenuRenderer(plugin, menuRegistry);
            menuService = new MenuService(plugin, menuRegistry, renderer, mandatorySessionStore, lang);
            actionExecutor = new MenuActionExecutor(plugin, menuService, renderer, menuRegistry, lang);
            menuService.setActionExecutor(actionExecutor);

            menuRegistry.registerContentProvider(new DemoPwarpContentProvider(false));
            menuRegistry.registerContentProvider(new DemoPwarpContentProvider(true));
            menuRegistry.registerContentProvider(new DemoMandatoryAfkProvider(menuService));

            mnguiCommand = new MnguiCommand(menuService, lang);
            registerLifecycleCommandsOnce(registrar -> registrar.register(
                    "mngui",
                    "MaliNess GUI demo komutlari.",
                    List.of(),
                    new MnguiBasicCommand(mnguiCommand)
            ));
        }

        menuRegistry.reload();
    }

    @Override
    protected void onEnable() {
    }

    @Override
    protected void onActivate() {
        registerListener(new MenuListener(plugin, menuService, menuRegistry, actionExecutor, lang));
        menuService.finishReload();
    }

    @Override
    protected void onDeactivate() {
        menuService.prepareReload();
        unregisterListener();
    }

    @Override
    protected void onDisable() {
        if (menuService != null && !plugin.isReloading()) {
            menuService.shutdown();
        }
    }

    public MenuService getMenuService() {
        return menuService;
    }
}
