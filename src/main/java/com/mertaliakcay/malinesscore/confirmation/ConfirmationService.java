package com.mertaliakcay.malinesscore.confirmation;

import com.mertaliakcay.malinesscore.MaliNessCore;
import com.mertaliakcay.malinesscore.messages.MessageService;
import com.mertaliakcay.malinesscore.messages.MessageType;
import com.mertaliakcay.malinesscore.util.PluginLang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class ConfirmationService {

    private static final long TIMEOUT_MS = 30_000L;

    private final MaliNessCore plugin;
    private final Map<UUID, PendingConfirmation> pending = new ConcurrentHashMap<>();

    public ConfirmationService(MaliNessCore plugin) {
        this.plugin = plugin;
    }

    public void request(Player player, Component prompt, Runnable onAccept, Runnable onDeny) {
        cancel(player.getUniqueId(), false);

        long expiresAt = System.currentTimeMillis() + TIMEOUT_MS;
        PendingConfirmation confirmation = new PendingConfirmation(expiresAt, onAccept, onDeny);
        pending.put(player.getUniqueId(), confirmation);

        PluginLang pluginLang = plugin.getPluginLang();
        MessageService messages = plugin.getMessageService();
        Component yes = messages.formatWithoutPrefix(MessageType.SUCCESS, "[/evet]")
                .clickEvent(ClickEvent.runCommand("/evet"))
                .hoverEvent(HoverEvent.showText(pluginLang.getPlain("confirm-button-yes-hover")));
        Component no = messages.formatWithoutPrefix(MessageType.ERROR, "[/hayır]")
                .clickEvent(ClickEvent.runCommand("/hayir"))
                .hoverEvent(HoverEvent.showText(pluginLang.getPlain("confirm-button-no-hover")));
        Component cancelButton = messages.formatWithoutPrefix(MessageType.WARNING, "[/iptal]")
                .clickEvent(ClickEvent.runCommand("/iptal"))
                .hoverEvent(HoverEvent.showText(pluginLang.getPlain("confirm-button-cancel-hover")));

        player.sendMessage(prompt);
        player.sendMessage(messages.prefix()
                .append(yes)
                .append(Component.space())
                .append(no)
                .append(Component.space())
                .append(cancelButton));
    }

    public boolean accept(Player player) {
        PendingConfirmation confirmation = getValid(player);
        if (confirmation == null) {
            return false;
        }

        try {
            confirmation.onAccept().run();
        } catch (Exception exception) {
            plugin.getLogger().log(Level.SEVERE, "Onay islemi tamamlanamadi: " + player.getName(), exception);
            plugin.getPluginLang().send(player, "confirm-failed");
            return false;
        }

        pending.remove(player.getUniqueId());
        plugin.getPluginLang().send(player, "confirm-accepted");
        return true;
    }

    public boolean deny(Player player) {
        PendingConfirmation confirmation = getValid(player);
        if (confirmation == null) {
            return false;
        }

        cancel(player.getUniqueId(), true);
        plugin.getPluginLang().send(player, "confirm-denied");
        return true;
    }

    public boolean cancel(Player player) {
        if (!pending.containsKey(player.getUniqueId())) {
            plugin.getPluginLang().send(player, "confirm-nothing-pending");
            return false;
        }

        cancel(player.getUniqueId(), true);
        plugin.getPluginLang().send(player, "confirm-cancelled");
        return true;
    }

    public void cancel(UUID playerId, boolean runDenyCallback) {
        PendingConfirmation confirmation = pending.remove(playerId);
        if (confirmation != null && runDenyCallback && confirmation.onDeny() != null) {
            confirmation.onDeny().run();
        }
    }

    public boolean hasPending(UUID playerId) {
        PendingConfirmation confirmation = pending.get(playerId);
        if (confirmation == null) {
            return false;
        }

        if (confirmation.isExpired()) {
            pending.remove(playerId);
            return false;
        }

        return true;
    }

    public void cancelAll() {
        for (UUID playerId : pending.keySet()) {
            cancel(playerId, true);
        }
    }

    private PendingConfirmation getValid(Player player) {
        PendingConfirmation confirmation = pending.get(player.getUniqueId());
        if (confirmation == null) {
            plugin.getPluginLang().send(player, "confirm-nothing-pending");
            return null;
        }

        if (confirmation.isExpired()) {
            cancel(player.getUniqueId(), true);
            plugin.getPluginLang().send(player, "confirm-expired");
            return null;
        }

        return confirmation;
    }
}
