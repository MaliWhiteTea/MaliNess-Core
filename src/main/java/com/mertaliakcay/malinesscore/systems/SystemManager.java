package com.mertaliakcay.malinesscore.systems;

import com.mertaliakcay.malinesscore.MaliNessCore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class SystemManager {

    private final MaliNessCore plugin;
    private final List<GameSystem> systems = new ArrayList<>();
    private final Set<String> activeSystems = new HashSet<>();

    public SystemManager(MaliNessCore plugin) {
        this.plugin = plugin;
    }

    public void register(GameSystem system) {
        systems.add(system);
    }

    public void enableAll() {
        List<String> enabled = new ArrayList<>();
        List<String> disabled = new ArrayList<>();
        activeSystems.clear();

        for (GameSystem system : systems) {
            try {
                system.enable(plugin);

                if (system instanceof AbstractGameSystem abstractSystem && abstractSystem.isActive()) {
                    enabled.add(system.getName());
                    activeSystems.add(system.getName());
                } else if (system instanceof AbstractGameSystem abstractSystem && !abstractSystem.isConfigEnabled()) {
                    disabled.add(system.getName());
                } else if (!(system instanceof AbstractGameSystem)) {
                    enabled.add(system.getName());
                    activeSystems.add(system.getName());
                }
            } catch (Exception e) {
                plugin.getPluginLang().logError(
                        "system-enable-failed",
                        "system", system.getName(),
                        "error", e.getMessage()
                );
                e.printStackTrace();
            }
        }

        plugin.getPluginLang().logInfo("systems-enabled-summary", "systems", formatSystemList(enabled));
        plugin.getPluginLang().logInfo("systems-disabled-summary", "systems", formatSystemList(disabled));
    }

    public void disableAll() {
        List<String> disabled = new ArrayList<>();

        for (int i = systems.size() - 1; i >= 0; i--) {
            GameSystem system = systems.get(i);
            try {
                system.disable();

                if (activeSystems.contains(system.getName())) {
                    disabled.add(system.getName());
                }
            } catch (Exception e) {
                plugin.getPluginLang().logError(
                        "system-disable-failed",
                        "system", system.getName(),
                        "error", e.getMessage()
                );
            }
        }

        activeSystems.clear();

        if (!disabled.isEmpty()) {
            plugin.getPluginLang().logInfo("systems-shutdown-summary", "systems", formatSystemList(disabled));
        }
    }

    public List<GameSystem> getSystems() {
        return Collections.unmodifiableList(systems);
    }

    public AbstractGameSystem findAbstractSystem(String systemId) {
        if (systemId == null) {
            return null;
        }

        for (GameSystem system : systems) {
            if (system instanceof AbstractGameSystem abstractSystem
                    && abstractSystem.getName().equalsIgnoreCase(systemId)) {
                return abstractSystem;
            }
        }

        return null;
    }

    public boolean setSystemEnabled(String systemId, boolean enabled) {
        AbstractGameSystem system = findAbstractSystem(systemId);
        if (system == null) {
            return false;
        }

        system.getConfig().get().set("enabled", enabled);
        system.getConfig().save();
        system.reload();

        if (system.isActive()) {
            activeSystems.add(system.getName());
        } else {
            activeSystems.remove(system.getName());
        }

        return true;
    }

    public void reloadAll() {
        List<String> enabled = new ArrayList<>();
        List<String> disabled = new ArrayList<>();
        activeSystems.clear();

        for (GameSystem system : systems) {
            try {
                if (system instanceof AbstractGameSystem abstractSystem) {
                    abstractSystem.reload();

                    if (abstractSystem.isActive()) {
                        enabled.add(system.getName());
                        activeSystems.add(system.getName());
                    } else if (!abstractSystem.isConfigEnabled()) {
                        disabled.add(system.getName());
                    }
                }
            } catch (Exception exception) {
                plugin.getPluginLang().logError(
                        "system-enable-failed",
                        "system", system.getName(),
                        "error", exception.getMessage()
                );
                exception.printStackTrace();
            }
        }

        plugin.getPluginLang().logInfo("systems-enabled-summary", "systems", formatSystemList(enabled));
        plugin.getPluginLang().logInfo("systems-disabled-summary", "systems", formatSystemList(disabled));
    }

    private String formatSystemList(List<String> names) {
        return names.isEmpty() ? "yok" : String.join(", ", names);
    }
}
