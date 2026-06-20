package com.mertaliakcay.malinesscore.util;

import com.mertaliakcay.malinesscore.MaliNessCore;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.regex.Pattern;

public final class MaliNessColorUtil {

    public static final String PERM_COLORS = "maliness-core.colors.use";

    private static final Pattern LEGACY_COLOR_PATTERN = Pattern.compile("(?i)&[0-9a-fk-orx]");
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("(?i)&#[0-9a-f]{6}");
    private static final Pattern SEMANTIC_COLOR_PATTERN = Pattern.compile("(?i)&z[hbcnvu]");
    private static final Pattern FORMATTING_PATTERN = Pattern.compile("(?i)&[lmnok]");

    private MaliNessColorUtil() {
    }

    public static boolean canUseColors(CommandSender sender) {
        return !(sender instanceof Player) || sender.hasPermission(PERM_COLORS);
    }

    public static String apply(String input, CommandSender sender, MaliNessCore plugin) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        if (!canUseColors(sender)) {
            return stripAllColors(input);
        }

        return translateSemanticColors(input, plugin.getConfig());
    }

    public static Component toComponent(String input, CommandSender sender, MaliNessCore plugin) {
        return ColorUtil.colorize(apply(input, sender, plugin));
    }

    public static String stripAllColors(String input) {
        String stripped = SEMANTIC_COLOR_PATTERN.matcher(input).replaceAll("");
        stripped = HEX_COLOR_PATTERN.matcher(stripped).replaceAll("");
        stripped = LEGACY_COLOR_PATTERN.matcher(stripped).replaceAll("");
        stripped = FORMATTING_PATTERN.matcher(stripped).replaceAll("");
        return stripped;
    }

    private static String translateSemanticColors(String input, FileConfiguration config) {
        String result = input;
        result = replaceSemantic(result, "&zh", config.getString("messages.colors.error", "&#ff1100"));
        result = replaceSemantic(result, "&zb", config.getString("messages.colors.success", "&#87d498"));
        result = replaceSemantic(result, "&zu", config.getString("messages.colors.warning", "&#ffd400"));
        result = replaceSemantic(result, "&zn", config.getString("messages.colors.normal", "&#f3e9e3"));
        result = replaceSemantic(result, "&zv", config.getString("messages.colors.token", "&#ffdb57"));
        return result;
    }

    private static String replaceSemantic(String input, String token, String color) {
        if (color == null || color.isEmpty()) {
            return input;
        }

        return input.replace(token, color).replace(token.toUpperCase(Locale.ROOT), color);
    }
}
