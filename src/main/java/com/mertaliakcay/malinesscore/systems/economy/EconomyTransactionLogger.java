package com.mertaliakcay.malinesscore.systems.economy;

import com.mertaliakcay.malinesscore.MaliNessCore;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;

public final class EconomyTransactionLogger {

    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MaliNessCore plugin;
    private final boolean enabled;

    public EconomyTransactionLogger(MaliNessCore plugin, boolean enabled) {
        this.plugin = plugin;
        this.enabled = enabled;
    }

    public void log(String type, String currency, String from, String to, String amount, String reason, String source) {
        if (!enabled) {
            return;
        }

        File folder = new File(plugin.getDataFolder(), "logs/economy");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File file = new File(folder, "transactions-" + LocalDate.now() + ".log");
        String line = TIMESTAMP.format(LocalDateTime.now())
                + " | " + type
                + " | " + currency
                + " | from=" + from
                + " | to=" + to
                + " | amount=" + amount
                + " | reason=" + reason
                + " | source=" + source;

        try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))) {
            writer.println(line);
        } catch (IOException exception) {
            plugin.getLogger().log(Level.WARNING, "Economy islem logu yazilamadi", exception);
        }
    }
}
