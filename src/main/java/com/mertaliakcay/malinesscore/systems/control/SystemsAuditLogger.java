package com.mertaliakcay.malinesscore.systems.control;

import com.mertaliakcay.malinesscore.MaliNessCore;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;

public final class SystemsAuditLogger {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MaliNessCore plugin;
    private final File auditFile;

    public SystemsAuditLogger(MaliNessCore plugin) {
        this.plugin = plugin;

        File logsFolder = new File(plugin.getDataFolder(), "logs");
        if (!logsFolder.exists()) {
            logsFolder.mkdirs();
        }

        this.auditFile = new File(logsFolder, "systems-audit.log");
    }

    public void log(String actorName, String actorId, String systemId, String action) {
        String line = timestamp()
                + " | actor=" + actorName
                + " | id=" + actorId
                + " | system=" + systemId
                + " | " + action;

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> append(line));
    }

    private void append(String line) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(auditFile, true))) {
            writer.println(line);
        } catch (IOException exception) {
            plugin.getLogger().log(Level.WARNING, "Sistem audit logu yazilamadi.", exception);
        }
    }

    private String timestamp() {
        return LocalDateTime.now().format(FORMATTER);
    }
}
