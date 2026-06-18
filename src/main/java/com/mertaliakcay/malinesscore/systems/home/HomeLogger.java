package com.mertaliakcay.malinesscore.systems.home;

import com.mertaliakcay.malinesscore.MaliNessCore;
import com.mertaliakcay.malinesscore.systems.home.model.HomeLocation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;

public final class HomeLogger {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MaliNessCore plugin;
    private final File playerLogFile;
    private final File adminLogFile;
    private final boolean logPlayerActions;
    private final boolean logAdminActions;

    public HomeLogger(MaliNessCore plugin, boolean logPlayerActions, boolean logAdminActions) {
        this.plugin = plugin;
        this.logPlayerActions = logPlayerActions;
        this.logAdminActions = logAdminActions;

        File logsFolder = new File(plugin.getDataFolder(), "logs");
        if (!logsFolder.exists()) {
            logsFolder.mkdirs();
        }

        this.playerLogFile = new File(logsFolder, "home-player.log");
        this.adminLogFile = new File(logsFolder, "home-admin.log");
    }

    public void logPlayer(String playerName, String playerId, String action, String homeName, HomeLocation location) {
        if (!logPlayerActions) {
            return;
        }

        append(playerLogFile, format(playerName, playerId, action, homeName, location, null, null));
    }

    public void logPlayerRename(String playerName, String playerId, String oldName, String newName) {
        if (!logPlayerActions) {
            return;
        }

        append(playerLogFile, timestamp()
                + " | uuid=" + playerId
                + " | name=" + playerName
                + " | RENAME | old=" + oldName
                + " | new=" + newName);
    }

    public void logAdmin(String adminName, String targetName, String targetId, String action, String homeName, HomeLocation location) {
        if (!logAdminActions) {
            return;
        }

        append(adminLogFile, format(targetName, targetId, action, homeName, location, adminName, "admin=" + adminName + " | target=" + targetName));
    }

    private String format(String playerName, String playerId, String action, String homeName, HomeLocation location, String adminName, String prefix) {
        StringBuilder builder = new StringBuilder(timestamp());
        if (prefix != null) {
            builder.append(" | ").append(prefix);
        } else {
            builder.append(" | uuid=").append(playerId).append(" | name=").append(playerName);
        }
        builder.append(" | ").append(action).append(" | home=").append(homeName);
        if (location != null) {
            builder.append(" | world=").append(location.getWorldName())
                    .append(" | x=").append((int) location.getX())
                    .append(" y=").append((int) location.getY())
                    .append(" z=").append((int) location.getZ());
        }
        return builder.toString();
    }

    private void append(File file, String line) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))) {
                writer.println(line);
            } catch (IOException exception) {
                plugin.getLogger().log(Level.WARNING, "Home log yazılamadı: " + file.getName(), exception);
            }
        });
    }

    private String timestamp() {
        return LocalDateTime.now().format(FORMATTER);
    }
}
