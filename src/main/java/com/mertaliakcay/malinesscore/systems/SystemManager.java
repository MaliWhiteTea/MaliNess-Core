package com.mertaliakcay.malinesscore.systems;

import com.mertaliakcay.malinesscore.MaliNessCore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SystemManager {

    private final MaliNessCore plugin;
    private final List<GameSystem> systems = new ArrayList<>();

    public SystemManager(MaliNessCore plugin) {
        this.plugin = plugin;
    }

    public void register(GameSystem system) {
        systems.add(system);
    }

    public void enableAll() {
        for (GameSystem system : systems) {
            try {
                system.enable(plugin);
                plugin.getPluginLang().logInfo(
                        "system-enabled",
                        "system", system.getName()
                );
            } catch (Exception e) {
                plugin.getPluginLang().logError(
                        "system-enable-failed",
                        "system", system.getName(),
                        "error", e.getMessage()
                );
                e.printStackTrace();
            }
        }
    }

    public void disableAll() {
        for (int i = systems.size() - 1; i >= 0; i--) {
            GameSystem system = systems.get(i);
            try {
                system.disable();
                plugin.getPluginLang().logInfo(
                        "system-disabled",
                        "system", system.getName()
                );
            } catch (Exception e) {
                plugin.getPluginLang().logError(
                        "system-disable-failed",
                        "system", system.getName(),
                        "error", e.getMessage()
                );
            }
        }
    }

    public List<GameSystem> getSystems() {
        return Collections.unmodifiableList(systems);
    }
}
