package com.mertaliakcay.malinesscore.gui.model;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class MenuItemDefinition {

    private final String id;
    private final int slot;
    private final Material material;
    private final int count;
    private final int customModelData;
    private final boolean enchantGlow;
    private final String name;
    private final List<String> lore;
    private final DynamicCountType dynamicCount;
    private final VisibilityRule visibilityRule;
    private final int visibilityMinPage;
    private final Map<MenuClickType, String> clicks;

    public MenuItemDefinition(
            String id,
            int slot,
            Material material,
            int count,
            int customModelData,
            boolean enchantGlow,
            String name,
            List<String> lore,
            DynamicCountType dynamicCount,
            VisibilityRule visibilityRule,
            int visibilityMinPage,
            Map<MenuClickType, String> clicks
    ) {
        this.id = id;
        this.slot = slot;
        this.material = material;
        this.count = count;
        this.customModelData = customModelData;
        this.enchantGlow = enchantGlow;
        this.name = name;
        this.lore = List.copyOf(lore);
        this.dynamicCount = dynamicCount;
        this.visibilityRule = visibilityRule;
        this.visibilityMinPage = visibilityMinPage;
        this.clicks = Map.copyOf(clicks);
    }

    public String getId() {
        return id;
    }

    public int getSlot() {
        return slot;
    }

    public Material getMaterial() {
        return material;
    }

    public int getCount() {
        return count;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public boolean isEnchantGlow() {
        return enchantGlow;
    }

    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return lore;
    }

    public DynamicCountType getDynamicCount() {
        return dynamicCount;
    }

    public VisibilityRule getVisibilityRule() {
        return visibilityRule;
    }

    public int getVisibilityMinPage() {
        return visibilityMinPage;
    }

    public Map<MenuClickType, String> getClicks() {
        return clicks;
    }

    public boolean isVisible(MenuSession session) {
        return switch (visibilityRule) {
            case ALWAYS -> true;
            case MIN_PAGE, HAS_PREV_PAGE -> session.getCurrentPage() >= visibilityMinPage;
            case HAS_NEXT_PAGE -> session.getCurrentPage() < session.getMaxPage();
        };
    }

    public ItemStack createBaseStack() {
        ItemStack stack = new ItemStack(material, Math.max(1, count));
        return stack;
    }
}
