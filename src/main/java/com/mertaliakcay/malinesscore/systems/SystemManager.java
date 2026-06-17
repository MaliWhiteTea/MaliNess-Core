package com.mertaliakcay.malinesscore.systems;

import com.mertaliakcay.malinesscore.MaliNessCore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public final class SystemManager {

    private final MaliNessCore plugin;
    private final Logger logger;
    private final List<GameSystem> systems = new ArrayList<>();

    public SystemManager(MaliNessCore plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void register(GameSystem system) {
        systems.add(system);
    }

    public void enableAll() {
        for (GameSystem system : systems) {
            try {
                system.enable(plugin);
                logger.info("[" + system.getName() + "] sistemi aktif edildi.");
            } catch (Exception e) {
                logger.severe("[" + system.getName() + "] sistemi aktif edilemedi: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void disableAll() {
        for (int i = systems.size() - 1; i >= 0; i--) {
            GameSystem system = systems.get(i);
            try {
                system.disable();
                logger.info("[" + system.getName() + "] sistemi kapatıldı.");
            } catch (Exception e) {
                logger.severe("[" + system.getName() + "] sistemi kapatılamadı: " + e.getMessage());
            }
        }
    }

    public List<GameSystem> getSystems() {
        return Collections.unmodifiableList(systems);
    }
}
