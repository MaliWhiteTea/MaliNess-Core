package com.mertaliakcay.malinesscore.systems.pwarp;

import com.mertaliakcay.malinesscore.MaliNessCore;
import com.mertaliakcay.malinesscore.systems.pwarp.model.Pwarp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;

public final class PwarpLogger {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MaliNessCore plugin;
    private final File playerLogFile;
    private final File adminLogFile;
    private final boolean logPlayerActions;
    private final boolean logAdminActions;

    public PwarpLogger(MaliNessCore plugin, boolean logPlayerActions, boolean logAdminActions) {
        this.plugin = plugin;
        this.logPlayerActions = logPlayerActions;
        this.logAdminActions = logAdminActions;

        File logsFolder = new File(plugin.getDataFolder(), "logs");
        if (!logsFolder.exists()) {
            logsFolder.mkdirs();
        }

        this.playerLogFile = new File(logsFolder, "pwarp-player.log");
        this.adminLogFile = new File(logsFolder, "pwarp-admin.log");
    }

    public void logTeleport(String playerName, String playerId, Pwarp pwarp) {
        if (!logPlayerActions) {
            return;
        }

        append(playerLogFile, timestamp()
                + " | uuid=" + playerId
                + " | name=" + playerName
                + " | TELEPORT | pwarp=" + pwarp.getName()
                + " | owner=" + pwarp.getOwnerName()
                + " | world=" + pwarp.getWorldName()
                + " | x=" + (int) pwarp.getX()
                + " y=" + (int) pwarp.getY()
                + " z=" + (int) pwarp.getZ());
    }

    public void logPlayer(String playerName, String playerId, String action, Pwarp pwarp) {
        if (!logPlayerActions) {
            return;
        }

        append(playerLogFile, timestamp()
                + " | uuid=" + playerId
                + " | name=" + playerName
                + " | " + action
                + " | pwarp=" + pwarp.getName());
    }

    public void logAdmin(String actorName, String action, Pwarp pwarp) {
        if (!logAdminActions) {
            return;
        }

        append(adminLogFile, timestamp()
                + " | admin=" + actorName
                + " | " + action
                + " | pwarp=" + pwarp.getName()
                + " | owner=" + pwarp.getOwnerName());
    }

    public void logAdminRename(String actorName, String oldName, String newName) {
        if (!logAdminActions) {
            return;
        }

        append(adminLogFile, timestamp()
                + " | admin=" + actorName
                + " | RENAME | old=" + oldName
                + " | new=" + newName);
    }

    private void append(File file, String line) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))) {
                writer.println(line);
            } catch (IOException exception) {
                plugin.getLogger().log(Level.WARNING, "Pwarp log yazılamadı: " + file.getName(), exception);
            }
        });
    }

    private String timestamp() {
        return LocalDateTime.now().format(FORMATTER);
    }
}
