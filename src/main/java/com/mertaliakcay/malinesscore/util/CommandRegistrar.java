package com.mertaliakcay.malinesscore.util;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

public final class CommandRegistrar {

    private CommandRegistrar() {
    }

    /**
     * Aynı isimli komutu başka bir eklenti veya Purpur devralmışsa,
     * MaliNess komutunu zorla kayıt eder.
     */
    public static void override(JavaPlugin plugin, String commandName, PluginCommand command) {
        unregister(commandName);

        for (String alias : command.getAliases()) {
            unregister(alias);
        }

        CommandMap commandMap = getCommandMap();
        if (commandMap == null) {
            plugin.getLogger().warning("CommandMap bulunamadı, komut override edilemedi: " + commandName);
            return;
        }

        commandMap.register(plugin.getName().toLowerCase(Locale.ROOT), command);
    }

    public static void unregister(String commandName) {
        Map<String, Command> knownCommands = getKnownCommands();
        if (knownCommands == null) {
            return;
        }

        knownCommands.remove(commandName.toLowerCase(Locale.ROOT));
    }

    private static CommandMap getCommandMap() {
        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            return (CommandMap) commandMapField.get(Bukkit.getServer());
        } catch (ReflectiveOperationException exception) {
            Bukkit.getLogger().log(Level.WARNING, "CommandMap okunamadı.", exception);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Command> getKnownCommands() {
        CommandMap commandMap = getCommandMap();
        if (commandMap == null) {
            return null;
        }

        try {
            Field knownCommandsField = commandMap.getClass().getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            return (Map<String, Command>) knownCommandsField.get(commandMap);
        } catch (ReflectiveOperationException exception) {
            Bukkit.getLogger().log(Level.WARNING, "knownCommands okunamadı.", exception);
            return null;
        }
    }
}
