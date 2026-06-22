package com.mertaliakcay.malinesscore.gui;

import com.mertaliakcay.malinesscore.gui.content.MenuContentProvider;
import com.mertaliakcay.malinesscore.gui.model.DynamicCountType;
import com.mertaliakcay.malinesscore.gui.model.MenuDefinition;
import com.mertaliakcay.malinesscore.gui.model.MenuItemDefinition;
import com.mertaliakcay.malinesscore.gui.model.MenuSession;
import com.mertaliakcay.malinesscore.gui.util.MenuItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class MenuRenderer {

    private final MenuRegistry registry;

    public MenuRenderer(MenuRegistry registry) {
        this.registry = registry;
    }

    public void render(MenuSession session) {
        MenuDefinition definition = session.getDefinition();
        Inventory inventory = session.getInventory();
        if (inventory == null) {
            return;
        }

        inventory.clear();
        session.clearContentEntries();

        MenuContentProvider provider = registry.getContentProvider(definition.getContentProviderId());
        if (provider != null) {
            session.setMaxPage(provider.getTotalPages(session));
            provider.populatePage(session);
            applyContent(session, provider);
        } else {
            session.setMaxPage(1);
        }

        applyChrome(session);
        applyFiller(session);
    }

    private void applyContent(MenuSession session, MenuContentProvider provider) {
        @SuppressWarnings("unchecked")
        List<Integer> override = (List<Integer>) session.getProviderState().get("content-slots-override");
        List<Integer> slots = override != null && !override.isEmpty()
                ? override
                : session.getDefinition().getContentSlots();
        Inventory inventory = session.getInventory();

        @SuppressWarnings("unchecked")
        List<ItemStack> pageItems = (List<ItemStack>) session.getProviderState().get("page-items");
        @SuppressWarnings("unchecked")
        List<String> entryIds = (List<String>) session.getProviderState().get("page-entry-ids");

        if (pageItems == null || entryIds == null) {
            return;
        }

        int limit = Math.min(slots.size(), pageItems.size());
        for (int index = 0; index < limit; index++) {
            int slot = slots.get(index);
            if (slot < 0 || slot >= inventory.getSize()) {
                continue;
            }
            ItemStack stack = pageItems.get(index);
            if (stack != null) {
                inventory.setItem(slot, stack.clone());
                session.getContentEntryBySlot().put(slot, entryIds.get(index));
            }
        }
    }

    private void applyChrome(MenuSession session) {
        Inventory inventory = session.getInventory();
        for (MenuItemDefinition item : session.getDefinition().getItems().values()) {
            if (!item.isVisible(session)) {
                continue;
            }

            int slot = item.getSlot();
            if (slot < 0 || slot >= inventory.getSize()) {
                continue;
            }

            int amount = resolveAmount(item, session);
            boolean prevButton = item.getDynamicCount() == DynamicCountType.PREV_REMAINING;
            ItemStack stack = MenuItemBuilder.build(
                    item.getMaterial(),
                    amount,
                    item.getCustomModelData(),
                    item.isEnchantGlow(),
                    item.getName(),
                    item.getLore(),
                    session,
                    prevButton
            );
            inventory.setItem(slot, stack);
        }
    }

    private void applyFiller(MenuSession session) {
        MenuDefinition definition = session.getDefinition();
        if (!definition.isFillerEnabled()) {
            return;
        }

        Material material = Material.matchMaterial(definition.getFillerMaterial());
        if (material == null) {
            material = Material.GRAY_STAINED_GLASS_PANE;
        }

        ItemStack filler = MenuItemBuilder.build(
                material,
                1,
                0,
                false,
                definition.getFillerName(),
                List.of(),
                session,
                false
        );

        Inventory inventory = session.getInventory();
        Set<Integer> occupied = new HashSet<>(session.getContentEntryBySlot().keySet());
        for (MenuItemDefinition item : definition.getItems().values()) {
            if (item.isVisible(session)) {
                occupied.add(item.getSlot());
            }
        }

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (!occupied.contains(slot)) {
                inventory.setItem(slot, filler.clone());
            }
        }
    }

    private int resolveAmount(MenuItemDefinition item, MenuSession session) {
        return switch (item.getDynamicCount()) {
            case PREV_REMAINING -> {
                int count = session.getPrevStackCount();
                yield count <= 0 ? 1 : count;
            }
            case NEXT_REMAINING -> {
                int count = session.getNextStackCount();
                yield count <= 0 ? 1 : count;
            }
            case NONE -> Math.max(1, item.getCount());
        };
    }
}
