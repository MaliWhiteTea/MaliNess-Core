package com.mertaliakcay.malinesscore.util;

import com.mertaliakcay.malinesscore.MaliNessCore;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Her sistem kendi lang dosyasını kullanır: langs/&lt;sistem&gt;.yml
 * Mesajlar & renk kodları ve &#RRGGBB hex renklerini destekler.
 */
public final class SystemLang {

    private final MaliNessCore plugin;
    private final String systemId;
    private File langFile;
    private FileConfiguration lang;

    public SystemLang(MaliNessCore plugin, String systemId) {
        this.plugin = plugin;
        this.systemId = systemId;
        reload();
    }

    public String getSystemId() {
        return systemId;
    }

    public FileConfiguration get() {
        return lang;
    }

    public void reload() {
        File folder = new File(plugin.getDataFolder(), "langs");
        if (!folder.exists() && !folder.mkdirs()) {
            plugin.getLogger().warning("langs klasörü oluşturulamadı.");
        }

        langFile = new File(folder, systemId + ".yml");
        if (!langFile.exists()) {
            String resourcePath = "langs/" + systemId + ".yml";
            if (plugin.getResource(resourcePath) != null) {
                plugin.saveResource(resourcePath, false);
            } else {
                try {
                    if (!langFile.createNewFile()) {
                        plugin.getLogger().warning("Lang dosyası oluşturulamadı: " + langFile.getName());
                    }
                } catch (IOException e) {
                    plugin.getLogger().log(Level.SEVERE, "Lang dosyası oluşturulamadı: " + langFile.getName(), e);
                }
            }
        }

        lang = YamlConfiguration.loadConfiguration(langFile);
    }

    public String getRaw(String key) {
        return lang.getString(key, key);
    }

    public Component get(String key) {
        return ColorUtil.colorize(applyPlaceholders(getRaw(key)));
    }

    public Component get(String key, Object... placeholders) {
        return ColorUtil.colorize(applyPlaceholders(getRaw(key), placeholders));
    }

    public void send(CommandSender sender, String key) {
        sender.sendMessage(get(key));
    }

    public void send(CommandSender sender, String key, Object... placeholders) {
        sender.sendMessage(get(key, placeholders));
    }

    public void sendActionBar(Player player, String key) {
        player.sendActionBar(get(key));
    }

    public void sendActionBar(Player player, String key, Object... placeholders) {
        player.sendActionBar(get(key, placeholders));
    }

    private String applyPlaceholders(String message) {
        String prefix = lang.getString("prefix", "");
        return message.replace("{prefix}", prefix);
    }

    private String applyPlaceholders(String message, Object[] placeholders) {
        String result = applyPlaceholders(message);

        if (placeholders.length % 2 != 0) {
            throw new IllegalArgumentException("Placeholder sayısı çift olmalı (anahtar, değer çiftleri).");
        }

        for (int i = 0; i < placeholders.length; i += 2) {
            String placeholderKey = String.valueOf(placeholders[i]);
            String placeholderValue = String.valueOf(placeholders[i + 1]);
            result = result.replace("{" + placeholderKey + "}", placeholderValue);
        }

        return result;
    }
}
