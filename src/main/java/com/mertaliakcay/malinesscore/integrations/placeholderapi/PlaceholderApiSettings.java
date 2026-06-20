package com.mertaliakcay.malinesscore.integrations.placeholderapi;

import com.mertaliakcay.malinesscore.MaliNessCore;
import org.bukkit.configuration.file.FileConfiguration;

public final class PlaceholderApiSettings {

    private boolean enabled = true;
    private boolean parseInMessages = true;
    private String onLabel = "Açık";
    private String offLabel = "Kapalı";
    private String yesLabel = "Evet";
    private String noLabel = "Hayır";

    public void reload(MaliNessCore plugin) {
        FileConfiguration config = plugin.getConfig();
        enabled = config.getBoolean("integrations.placeholderapi.enabled", true);
        parseInMessages = config.getBoolean("integrations.placeholderapi.parse-in-messages", true);
        onLabel = config.getString("integrations.placeholderapi.labels.on", onLabel);
        offLabel = config.getString("integrations.placeholderapi.labels.off", offLabel);
        yesLabel = config.getString("integrations.placeholderapi.labels.yes", yesLabel);
        noLabel = config.getString("integrations.placeholderapi.labels.no", noLabel);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isParseInMessages() {
        return parseInMessages;
    }

    public String onLabel() {
        return onLabel;
    }

    public String offLabel() {
        return offLabel;
    }

    public String yesLabel() {
        return yesLabel;
    }

    public String noLabel() {
        return noLabel;
    }
}
