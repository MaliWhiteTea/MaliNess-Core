package com.mertaliakcay.malinesscore.gui.util;

import com.mertaliakcay.malinesscore.MaliNessCore;
import com.mertaliakcay.malinesscore.gui.model.MenuSession;
import com.mertaliakcay.malinesscore.systems.economy.EconomyConstants;
import com.mertaliakcay.malinesscore.systems.economy.EconomyService;
import com.mertaliakcay.malinesscore.util.ColorUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

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
            boolean prevButton,
            MaliNessCore plugin,
            Player player
    ) {
        ItemStack stack = new ItemStack(material, Math.max(1, amount));
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return stack;
        }

        if (name != null && !name.isBlank()) {
            meta.displayName(applyPlaceholders(name, session, player, plugin, prevButton));
        }

        if (loreLines != null && !loreLines.isEmpty()) {
            List<Component> lore = new ArrayList<>();
            for (String line : loreLines) {
                lore.add(applyPlaceholders(line, session, player, plugin, prevButton));
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
        return applyPlaceholders(text, session, null, null, prevButton);
    }

    public static Component applyPlaceholders(
            String text,
            MenuSession session,
            Player player,
            MaliNessCore plugin,
            boolean prevButton
    ) {
        if (text == null) {
            return Component.empty();
        }
        String replaced = applyPlaceholdersPlain(text, session, player, plugin, prevButton);
        return ColorUtil.colorize(replaced);
    }

    public static String applyPlaceholdersPlain(String text, MenuSession session) {
        return applyPlaceholdersPlain(text, session, null, null, false);
    }

    public static String applyPlaceholdersPlain(String text, MenuSession session, Player player, MaliNessCore plugin) {
        return applyPlaceholdersPlain(text, session, player, plugin, false);
    }

    public static String applyPlaceholdersPlain(
            String text,
            MenuSession session,
            Player player,
            MaliNessCore plugin,
            boolean prevButton
    ) {
        if (text == null || session == null) {
            return text;
        }

        String replaced = text
                .replace("{page}", String.valueOf(session.getCurrentPage()))
                .replace("{max_page}", String.valueOf(session.getMaxPage()))
                .replace("{target_page}", String.valueOf(prevButton ? session.getPrevTargetPage() : session.getNextTargetPage()))
                .replace("{pages_back}", String.valueOf(Math.max(0, session.getCurrentPage() - 1)))
                .replace("{pages_forward}", String.valueOf(Math.max(0, session.getMaxPage() - session.getCurrentPage())));

        if (plugin != null && player != null) {
            EconomyService economyService = plugin.getEconomyService();
            if (economyService != null && economyService.isAvailable()) {
                replaced = replaced
                        .replace("{balance}", economyService.formatPrimary(
                                economyService.getBalance(player, EconomyConstants.PRIMARY_CURRENCY)))
                        .replace("{balance_tl}", economyService.format(
                                economyService.getBalance(player, EconomyConstants.PRIMARY_CURRENCY),
                                EconomyConstants.PRIMARY_CURRENCY))
                        .replace("{balance_cosmetic}", economyService.format(
                                economyService.getBalance(player, EconomyConstants.COSMETIC_CURRENCY),
                                EconomyConstants.COSMETIC_CURRENCY));
            }
            if (plugin.getPlaceholderApiIntegration().isAvailable()) {
                replaced = plugin.getPlaceholderApiIntegration().applyPlaceholders(player, replaced);
            }
        }
        return replaced;
    }
}
