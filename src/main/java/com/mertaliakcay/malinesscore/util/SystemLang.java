package com.mertaliakcay.malinesscore.util;

import com.mertaliakcay.malinesscore.MaliNessCore;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Her sistem kendi lang dosyasını kullanır: langs/&lt;sistem&gt;.yml
 * Örnek: langs/tpa.yml, langs/home.yml
 */
public final class SystemLang extends AbstractLang {

    private final String systemId;
    private File langFile;

    public SystemLang(MaliNessCore plugin, String systemId) {
        super(plugin);
        this.systemId = systemId;
        reload();
    }

    public String getSystemId() {
        return systemId;
    }

    @Override
    public void reload() {
        File folder = new File(plugin.getDataFolder(), "langs");
        if (!folder.exists() && !folder.mkdirs()) {
            plugin.getLogger().warning("langs klasörü oluşturulamadı.");
        }

        langFile = new File(folder, systemId + ".yml");
        String resourcePath = "langs/" + systemId + ".yml";

        if (plugin.getResource(resourcePath) != null) {
            lang = hasLangVersionInResource(resourcePath)
                    ? YamlMerger.loadAndMergeLang(plugin, langFile, resourcePath)
                    : YamlMerger.loadAndMerge(plugin, langFile, resourcePath);
        } else {
            if (!langFile.exists()) {
                try {
                    if (!langFile.createNewFile()) {
                        plugin.getLogger().warning("Lang dosyası oluşturulamadı: " + langFile.getName());
                    }
                } catch (IOException exception) {
                    plugin.getLogger().log(Level.SEVERE, "Lang dosyası oluşturulamadı: " + langFile.getName(), exception);
                }
            }
            lang = YamlConfiguration.loadConfiguration(langFile);
        }
    }

    private boolean hasLangVersionInResource(String resourcePath) {
        try (var stream = plugin.getResource(resourcePath)) {
            if (stream == null) {
                return false;
            }
            return YamlConfiguration.loadConfiguration(
                    new java.io.InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8)
            ).contains("lang-version");
        } catch (Exception exception) {
            plugin.getLogger().log(Level.WARNING, "Lang sürümü okunamadı: " + resourcePath, exception);
            return false;
        }
    }
}
