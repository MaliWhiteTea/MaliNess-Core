package com.mertaliakcay.malinesscore.gui;

import com.mertaliakcay.malinesscore.MaliNessCore;
import com.mertaliakcay.malinesscore.gui.content.MenuContentProvider;
import com.mertaliakcay.malinesscore.gui.loader.MenuYamlLoader;
import com.mertaliakcay.malinesscore.gui.model.MenuClickProtectionSettings;
import com.mertaliakcay.malinesscore.gui.model.MenuDefinition;
import com.mertaliakcay.malinesscore.util.YamlMerger;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

public final class MenuRegistry {

    private final MaliNessCore plugin;
    private final Map<String, MenuDefinition> menus = new LinkedHashMap<>();
    private final Map<String, MenuContentProvider> contentProviders = new LinkedHashMap<>();
    private YamlConfiguration globalConfig;
    private MenuYamlLoader loader;

    public MenuRegistry(MaliNessCore plugin) {
        this.plugin = plugin;
    }

    public void registerContentProvider(MenuContentProvider provider) {
        contentProviders.put(provider.getId(), provider);
    }

    public MenuContentProvider getContentProvider(String id) {
        return id == null ? null : contentProviders.get(id);
    }

    public void reload() {
        menus.clear();
        loadGlobalConfig();
        loader = new MenuYamlLoader(globalConfig);
        copyDefaultMenus();
        loadMenusFromDisk();
    }

    public MenuDefinition getMenu(String id) {
        return menus.get(id);
    }

    public Collection<String> getMenuIds() {
        return Collections.unmodifiableCollection(menus.keySet());
    }

    public YamlConfiguration getGlobalConfig() {
        return globalConfig;
    }

    public boolean isEnabled() {
        return globalConfig != null && globalConfig.getBoolean("enabled", true);
    }

    public boolean isCloseOnOtherInventory() {
        return globalConfig == null || globalConfig.getBoolean("close-on-other-inventory", true);
    }

    public boolean isPickupWhileMenuOpen() {
        return globalConfig == null || globalConfig.getBoolean("pickup-while-menu-open", true);
    }

    public boolean isMandatoryReleaseLogging() {
        return globalConfig != null && globalConfig.getBoolean("logging.mandatory-release", true);
    }

    public MenuClickProtectionSettings getClickProtection() {
        if (globalConfig == null) {
            return new MenuClickProtectionSettings(true, true, 350L);
        }
        var section = globalConfig.getConfigurationSection("click-protection");
        boolean doubleClickGuard = section == null || section.getBoolean("double-click-guard", true);
        var cooldown = section != null ? section.getConfigurationSection("button-cooldown") : null;
        boolean cooldownEnabled = cooldown == null || cooldown.getBoolean("enabled", true);
        long duration = cooldown != null ? cooldown.getLong("duration-ms", 350L) : 350L;
        return new MenuClickProtectionSettings(doubleClickGuard, cooldownEnabled, duration);
    }

    private void loadGlobalConfig() {
        File configFolder = new File(plugin.getDataFolder(), "configs");
        if (!configFolder.exists()) {
            configFolder.mkdirs();
        }

        File file = new File(configFolder, "gui.yml");
        globalConfig = YamlMerger.loadAndMerge(plugin, file, "configs/gui.yml");
    }

    private void copyDefaultMenus() {
        File guisFolder = new File(plugin.getDataFolder(), "guis");
        if (!guisFolder.exists() && !guisFolder.mkdirs()) {
            plugin.getLogger().warning("guis klasörü oluşturulamadı.");
        }

        List<String> resources = listGuiResources();
        for (String resourcePath : resources) {
            String fileName = resourcePath.substring(resourcePath.lastIndexOf('/') + 1);
            File target = new File(guisFolder, fileName);
            if (!target.exists()) {
                try (InputStream stream = plugin.getResource(resourcePath)) {
                    if (stream != null) {
                        java.nio.file.Files.copy(stream, target.toPath());
                    }
                } catch (IOException exception) {
                    plugin.getLogger().log(Level.SEVERE, "GUI dosyası kopyalanamadı: " + fileName, exception);
                }
            } else {
                YamlMerger.mergeIntoExisting(plugin, target, resourcePath);
            }
        }
    }

    private List<String> listGuiResources() {
        return List.of(
                "guis/demo-pwarp-layout.yml",
                "guis/demo-pwarp-empty.yml",
                "guis/demo-mandatory-afk.yml",
                "guis/demo-overlay-locked.yml",
                "guis/demo-overlay-allowed.yml",
                "guis/demo-hopper.yml",
                "guis/demo-dropper.yml",
                "guis/demo-click-test.yml",
                "guis/demo-economy-shop.yml",
                "guis/demo-economy-hopper.yml"
        );
    }

    private void loadMenusFromDisk() {
        File guisFolder = new File(plugin.getDataFolder(), "guis");
        File[] files = guisFolder.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".yml"));
        if (files == null) {
            return;
        }

        for (File file : files) {
            try {
                YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
                MenuDefinition definition = loader.load(yaml);
                menus.put(definition.getId(), definition);
            } catch (Exception exception) {
                plugin.getLogger().log(Level.SEVERE, "GUI yüklenemedi: " + file.getName(), exception);
            }
        }
    }
}
