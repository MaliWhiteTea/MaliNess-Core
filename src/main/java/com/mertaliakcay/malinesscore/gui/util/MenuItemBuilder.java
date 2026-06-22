package com.mertaliakcay.malinesscore.gui.util;

import com.mertaliakcay.malinesscore.gui.model.MenuSession;
import com.mertaliakcay.malinesscore.util.ColorUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MenuItemBuilder {

    private MenuItemBuilder() {
    }

    public static ItemStack build(
            Material material,
            int amount,
            int customModelData,
            boolean enchantGlow,
            String name,
            List<String> loreLines,
            MenuSession session,
            boolean prevButton
    ) {
        ItemStack stack = new ItemStack(material, Math.max(1, amount));
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return stack;
        }

        if (name != null && !name.isBlank()) {
            meta.displayName(applyPlaceholders(name, session, prevButton));
        }

        if (loreLines != null && !loreLines.isEmpty()) {
            List<Component> lore = new ArrayList<>();
            for (String line : loreLines) {
                lore.add(applyPlaceholders(line, session, prevButton));
            }
            meta.lore(lore);
        }

        if (customModelData > 0) {
            meta.setCustomModelData(customModelData);
        }

        if (enchantGlow) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        stack.setItemMeta(meta);
        return stack;
    }

    public static Component applyPlaceholders(String text, MenuSession session, boolean prevButton) {
        if (text == null) {
            return Component.empty();
        }
        if (session == null) {
            return ColorUtil.colorize(text);
        }

        Map<String, String> values = Map.of(
                "page", String.valueOf(session.getCurrentPage()),
                "max_page", String.valueOf(session.getMaxPage()),
                "target_page", String.valueOf(prevButton ? session.getPrevTargetPage() : session.getNextTargetPage()),
                "pages_back", String.valueOf(Math.max(0, session.getCurrentPage() - 1)),
                "pages_forward", String.valueOf(Math.max(0, session.getMaxPage() - session.getCurrentPage()))
        );

        String replaced = text;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            replaced = replaced.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return ColorUtil.colorize(replaced);
    }

    public static String applyPlaceholdersPlain(String text, MenuSession session) {
        if (text == null || session == null) {
            return text;
        }
        return text
                .replace("{page}", String.valueOf(session.getCurrentPage()))
                .replace("{max_page}", String.valueOf(session.getMaxPage()))
                .replace("{target_page}", String.valueOf(session.getNextTargetPage()))
                .replace("{pages_back}", String.valueOf(Math.max(0, session.getCurrentPage() - 1)))
                .replace("{pages_forward}", String.valueOf(Math.max(0, session.getMaxPage() - session.getCurrentPage())));
    }
}
