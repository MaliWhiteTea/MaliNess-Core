package com.mertaliakcay.malinesscore.util;

import com.mertaliakcay.malinesscore.MaliNessCore;
import com.mertaliakcay.malinesscore.messages.MessageType;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public abstract class AbstractLang {

    protected final MaliNessCore plugin;
    protected FileConfiguration lang;

    protected AbstractLang(MaliNessCore plugin) {
        this.plugin = plugin;
    }

    public abstract void reload();

    public FileConfiguration get() {
        return lang;
    }

    public MessageType getType(String key) {
        String type = lang.getString(key + ".type");
        return MessageType.fromString(type != null ? type : "normal");
    }

    public String getText(String key) {
        String text = lang.getString(key + ".text");
        if (text != null) {
            return text;
        }
        return lang.getString(key, key);
    }

    public Component get(String key, Object... placeholders) {
        return plugin.getMessageService().format(getType(key), getText(key), placeholders);
    }

    public void send(CommandSender sender, String key, Object... placeholders) {
        sender.sendMessage(get(key, placeholders));
    }

    public void sendActionBar(Player player, String key, Object... placeholders) {
        player.sendActionBar(get(key, placeholders));
    }

    public void logInfo(String key, Object... placeholders) {
        plugin.getMessageService().logInfo(getType(key), getText(key), placeholders);
    }

    public void logWarn(String key, Object... placeholders) {
        plugin.getMessageService().logWarn(getType(key), getText(key), placeholders);
    }

    public void logError(String key, Object... placeholders) {
        plugin.getMessageService().logError(getType(key), getText(key), placeholders);
    }
}
