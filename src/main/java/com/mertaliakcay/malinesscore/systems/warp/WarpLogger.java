package com.mertaliakcay.malinesscore.systems.warp;

import com.mertaliakcay.malinesscore.MaliNessCore;
import com.mertaliakcay.malinesscore.systems.warp.model.Warp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;

public final class WarpLogger {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MaliNessCore plugin;
    private final File playerLogFile;
    private final File adminLogFile;
    private final boolean logPlayerActions;
    private final boolean logAdminActions;

    public WarpLogger(MaliNessCore plugin, boolean logPlayerActions, boolean logAdminActions) {
        this.plugin = plugin;
        this.logPlayerActions = logPlayerActions;
        this.logAdminActions = logAdminActions;

        File logsFolder = new File(plugin.getDataFolder(), "logs");
        if (!logsFolder.exists()) {
            logsFolder.mkdirs();
        }

        this.playerLogFile = new File(logsFolder, "warp-player.log");
        this.adminLogFile = new File(logsFolder, "warp-admin.log");
    }

    public void logTeleport(String playerName, String playerId, Warp warp) {
        if (!logPlayerActions) {
            return;
        }

        append(playerLogFile, timestamp()
                + " | uuid=" + playerId
                + " | name=" + playerName
                + " | TELEPORT | warp=" + warp.getName()
                + " | world=" + warp.getWorldName()
                + " | x=" + (int) warp.getX()
                + " y=" + (int) warp.getY()
                + " z=" + (int) warp.getZ());
    }

    public void logAdmin(String adminName, String action, Warp warp) {
        if (!logAdminActions) {
            return;
        }

        StringBuilder builder = new StringBuilder(timestamp())
                .append(" | admin=").append(adminName)
                .append(" | ").append(action)
                .append(" | warp=").append(warp.getName());

        if (warp.getWorldName() != null) {
            builder.append(" | world=").append(warp.getWorldName())
                    .append(" | x=").append((int) warp.getX())
                    .append(" y=").append((int) warp.getY())
                    .append(" z=").append((int) warp.getZ());
        }

        append(adminLogFile, builder.toString());
    }

    public void logAdminRename(String adminName, String oldName, String newName) {
        if (!logAdminActions) {
            return;
        }

        append(adminLogFile, timestamp()
                + " | admin=" + adminName
                + " | RENAME | old=" + oldName
                + " | new=" + newName);
    }

    public void logAdminDescription(String adminName, String warpName, String description) {
        if (!logAdminActions) {
            return;
        }

        append(adminLogFile, timestamp()
                + " | admin=" + adminName
                + " | DESCRIPTION | warp=" + warpName
                + " | text=" + description);
    }

    private void append(File file, String line) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))) {
                writer.println(line);
            } catch (IOException exception) {
                plugin.getLogger().log(Level.WARNING, "Warp log yazılamadı: " + file.getName(), exception);
            }
        });
    }

    private String timestamp() {
        return LocalDateTime.now().format(FORMATTER);
    }
}
