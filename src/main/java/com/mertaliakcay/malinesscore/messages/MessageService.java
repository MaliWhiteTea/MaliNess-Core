package com.mertaliakcay.malinesscore.messages;

import com.mertaliakcay.malinesscore.MaliNessCore;
import com.mertaliakcay.malinesscore.util.ColorUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MessageService {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^}]+)}");

    private final MaliNessCore plugin;

    private String prefix = "&#992fe0ᴍᴀʟɪɴᴇꜱꜱ ɴᴇᴛᴡᴏʀᴋ &f| ";
    private String warningColor = "&#ffd400";
    private String errorColor = "&#ff1100";
    private String normalColor = "&#f3e9e3";
    private String successColor = "&#87d498";
    private String tokenColor = "&#ffdb57";

    public MessageService(MaliNessCore plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        FileConfiguration config = plugin.getConfig();

        prefix = config.getString("messages.prefix", prefix);
        warningColor = config.getString("messages.colors.warning", warningColor);
        errorColor = config.getString("messages.colors.error", errorColor);
        normalColor = config.getString("messages.colors.normal", normalColor);
        successColor = config.getString("messages.colors.success", successColor);
        tokenColor = config.getString("messages.colors.token", tokenColor);
    }

    public Component format(MessageType type, String template, Object... placeholders) {
        return formatInternal(true, type, template, placeholders);
    }

    public Component formatWithoutPrefix(MessageType type, String template, Object... placeholders) {
        return formatInternal(false, type, template, placeholders);
    }

    public Component prefix() {
        return ColorUtil.colorize(prefix);
    }

    private Component formatInternal(boolean includePrefix, MessageType type, String template, Object... placeholders) {
        Map<String, String> values = toPlaceholderMap(placeholders);
        String typeColor = getTypeColor(type);
        StringBuilder builder = new StringBuilder(prefix.length() + template.length() + 32);
        if (includePrefix) {
            builder.append(prefix);
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        int lastIndex = 0;

        while (matcher.find()) {
            if (matcher.start() > lastIndex) {
                builder.append(typeColor).append(template, lastIndex, matcher.start());
            }

            String key = matcher.group(1);
            String value = values.getOrDefault(key, "{" + key + "}");
            builder.append(tokenColor).append(value);
            lastIndex = matcher.end();
        }

        if (lastIndex < template.length()) {
            builder.append(typeColor).append(template.substring(lastIndex));
        }

        return ColorUtil.colorize(builder.toString());
    }

    public void send(CommandSender sender, MessageType type, String template, Object... placeholders) {
        sender.sendMessage(format(type, template, placeholders));
    }

    public void logInfo(MessageType type, String template, Object... placeholders) {
        plugin.getComponentLogger().info(format(type, template, placeholders));
    }

    public void logWarn(MessageType type, String template, Object... placeholders) {
        plugin.getComponentLogger().warn(format(type, template, placeholders));
    }

    public void logError(MessageType type, String template, Object... placeholders) {
        plugin.getComponentLogger().error(format(type, template, placeholders));
    }

    private String getTypeColor(MessageType type) {
        return switch (type) {
            case WARNING -> warningColor;
            case ERROR -> errorColor;
            case SUCCESS -> successColor;
            case NORMAL -> normalColor;
        };
    }

    private Map<String, String> toPlaceholderMap(Object... placeholders) {
        if (placeholders.length % 2 != 0) {
            throw new IllegalArgumentException("Placeholder sayısı çift olmalı (anahtar, değer çiftleri).");
        }

        Map<String, String> values = new HashMap<>();
        for (int i = 0; i < placeholders.length; i += 2) {
            values.put(String.valueOf(placeholders[i]), String.valueOf(placeholders[i + 1]));
        }
        return values;
    }
}
