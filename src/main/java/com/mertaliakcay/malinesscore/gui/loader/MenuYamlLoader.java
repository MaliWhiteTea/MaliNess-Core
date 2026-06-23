package com.mertaliakcay.malinesscore.gui.loader;

import com.mertaliakcay.malinesscore.gui.model.ClosePolicy;
import com.mertaliakcay.malinesscore.gui.model.DynamicCountType;
import com.mertaliakcay.malinesscore.gui.model.MenuClickType;
import com.mertaliakcay.malinesscore.gui.model.EconomyMenuBehavior;
import com.mertaliakcay.malinesscore.gui.model.MenuDefinition;
import com.mertaliakcay.malinesscore.gui.model.EconomyOutcome;
import com.mertaliakcay.malinesscore.gui.model.MenuItemDefinition;
import com.mertaliakcay.malinesscore.gui.model.PlayerInventoryPolicy;
import com.mertaliakcay.malinesscore.gui.model.VisibilityRule;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class MenuYamlLoader {

    private final YamlConfiguration globalDefaults;

    public MenuYamlLoader(YamlConfiguration globalDefaults) {
        this.globalDefaults = globalDefaults;
    }

    public MenuDefinition load(YamlConfiguration yaml) {
        String id = yaml.getString("id");
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Menu id eksik.");
        }

        InventoryType type = parseType(yaml.getString("type", "CHEST"));
        int rows = yaml.getInt("rows", 6);
        int chestSize = Math.clamp(rows, 1, 6) * 9;

        ConfigurationSection defaults = globalDefaults.getConfigurationSection("defaults");
        String title = yaml.getString("title", "&8Menu");
        String permission = yaml.getString("permission", "maliness-core.gui." + id);

        PlayerInventoryPolicy playerInventory = PlayerInventoryPolicy.fromString(
                yaml.getString("player-inventory", defaults != null ? defaults.getString("player-inventory") : "locked")
        );
        ClosePolicy closePolicy = ClosePolicy.fromString(
                yaml.getString("close-policy", defaults != null ? defaults.getString("close-policy") : "normal")
        );
        boolean closeOnCommand = yaml.getBoolean(
                "close-on-command",
                defaults != null && defaults.getBoolean("close-on-command", false)
        );
        boolean escapeOnDeath = yaml.getBoolean(
                "escape-on-death",
                defaults != null && defaults.getBoolean("escape-on-death", false)
        );
        boolean escapeOnQuit = yaml.getBoolean(
                "escape-on-quit",
                defaults == null || defaults.getBoolean("escape-on-quit", true)
        );
        boolean blockMovement = yaml.getBoolean(
                "block-movement",
                defaults != null && defaults.getBoolean("block-movement", true)
        );
        boolean allowDamage = yaml.getBoolean(
                "allow-damage",
                defaults == null || defaults.getBoolean("allow-damage", true)
        );
        boolean allowChat = yaml.getBoolean(
                "allow-chat",
                defaults == null || !defaults.getBoolean("allow-chat", false)
        );

        ConfigurationSection fillerSection = yaml.getConfigurationSection("filler");
        boolean fillerEnabled = fillerSection != null && fillerSection.getBoolean("enabled", false);
        String fillerMaterial = fillerSection != null ? fillerSection.getString("material", "GRAY_STAINED_GLASS_PANE") : "GRAY_STAINED_GLASS_PANE";
        String fillerName = fillerSection != null ? fillerSection.getString("name", " ") : " ";

        List<Integer> contentSlots = yaml.getIntegerList("layout.content-slots");
        if (contentSlots.isEmpty()) {
            ConfigurationSection layout = yaml.getConfigurationSection("layout");
            if (layout != null) {
                contentSlots = layout.getIntegerList("content-slots");
            }
        }

        String contentProvider = yaml.getString("content-provider");
        Map<String, MenuItemDefinition> items = loadItems(yaml.getConfigurationSection("items"));
        EconomyMenuBehavior economyBehavior = loadEconomyBehavior(yaml);

        return new MenuDefinition(
                id,
                type,
                chestSize,
                title,
                permission,
                playerInventory,
                closePolicy,
                closeOnCommand,
                escapeOnDeath,
                escapeOnQuit,
                blockMovement,
                allowDamage,
                allowChat,
                fillerEnabled,
                fillerMaterial,
                fillerName,
                contentSlots,
                contentProvider,
                items,
                economyBehavior
        );
    }

    private EconomyMenuBehavior loadEconomyBehavior(YamlConfiguration yaml) {
        ConfigurationSection defaults = globalDefaults.getConfigurationSection("economy-defaults");
        EconomyOutcome defaultInsufficient = EconomyOutcome.fromString(
                defaults != null ? defaults.getString("on-insufficient") : null,
                EconomyOutcome.CLOSE
        );
        EconomyOutcome defaultSuccess = EconomyOutcome.fromString(
                defaults != null ? defaults.getString("on-success") : null,
                EconomyOutcome.CLOSE
        );
        EconomyOutcome defaultError = EconomyOutcome.fromString(
                defaults != null ? defaults.getString("on-error") : null,
                EconomyOutcome.CLOSE
        );

        ConfigurationSection section = yaml.getConfigurationSection("economy-behavior");
        if (section == null) {
            return new EconomyMenuBehavior(defaultInsufficient, defaultSuccess, defaultError);
        }

        return new EconomyMenuBehavior(
                EconomyOutcome.fromString(section.getString("on-insufficient"), defaultInsufficient),
                EconomyOutcome.fromString(section.getString("on-success"), defaultSuccess),
                EconomyOutcome.fromString(section.getString("on-error"), defaultError)
        );
    }

    private Map<String, MenuItemDefinition> loadItems(ConfigurationSection section) {
        Map<String, MenuItemDefinition> items = new LinkedHashMap<>();
        if (section == null) {
            return items;
        }

        for (String itemId : section.getKeys(false)) {
            ConfigurationSection itemSection = section.getConfigurationSection(itemId);
            if (itemSection == null) {
                continue;
            }

            Material material = Material.matchMaterial(itemSection.getString("material", "STONE").toUpperCase(Locale.ROOT));
            if (material == null) {
                material = Material.STONE;
            }

            Map<MenuClickType, String> clicks = new EnumMap<>(MenuClickType.class);
            ConfigurationSection clicksSection = itemSection.getConfigurationSection("clicks");
            if (clicksSection != null) {
                for (String clickKey : clicksSection.getKeys(false)) {
                    clicks.put(MenuClickType.fromYamlKey(clickKey), clicksSection.getString(clickKey, "noop"));
                }
            } else if (itemSection.contains("action")) {
                clicks.put(MenuClickType.LEFT, itemSection.getString("action", "noop"));
            }

            String visibility = itemSection.getString("visibility", "always");
            VisibilityRule visibilityRule = VisibilityRule.parse(visibility);
            int minPage = VisibilityRule.parseMinPage(visibility);

            items.put(itemId, new MenuItemDefinition(
                    itemId,
                    itemSection.getInt("slot", 0),
                    material,
                    itemSection.getInt("count", 1),
                    itemSection.getInt("custom_model_data", 0),
                    itemSection.getBoolean("enchant-glow", false),
                    itemSection.getString("name"),
                    itemSection.getStringList("lore"),
                    DynamicCountType.fromString(itemSection.getString("dynamic-count")),
                    visibilityRule,
                    minPage,
                    clicks
            ));
        }

        return items;
    }

    private InventoryType parseType(String raw) {
        if (raw == null) {
            return InventoryType.CHEST;
        }
        return switch (raw.toUpperCase(Locale.ROOT)) {
            case "HOPPER" -> InventoryType.HOPPER;
            case "DROPPER" -> InventoryType.DROPPER;
            default -> InventoryType.CHEST;
        };
    }
}
