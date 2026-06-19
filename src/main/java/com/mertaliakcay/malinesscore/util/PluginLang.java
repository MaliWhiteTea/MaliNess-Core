package com.mertaliakcay.malinesscore.util;

import com.mertaliakcay.malinesscore.MaliNessCore;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

/**
 * Eklentinin ana mesaj dosyası: pluginlang.yml
 */
public final class PluginLang extends AbstractLang {

    private File langFile;

    public PluginLang(MaliNessCore plugin) {
        super(plugin);
        reload();
    }

    @Override
    public void reload() {
        langFile = new File(plugin.getDataFolder(), "pluginlang.yml");
        lang = YamlMerger.loadAndMergeLang(plugin, langFile, "pluginlang.yml");
    }
}
