package com.mertaliakcay.malinesscore.confirmation;

import com.mertaliakcay.malinesscore.MaliNessCore;
import com.mertaliakcay.malinesscore.util.PluginLang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class ConfirmationService {

    private static final long TIMEOUT_MS = 30_000L;

    private final MaliNessCore plugin;
    private final Map<UUID, PendingConfirmation> pending = new ConcurrentHashMap<>();

    public ConfirmationService(MaliNessCore plugin) {
        this.plugin = plugin;
    }

    public void request(Player player, Component prompt, Runnable onAccept, Runnable onDeny) {
        cancel(player.getUniqueId(), false);

        String token = generateToken();
        long expiresAt = System.currentTimeMillis() + TIMEOUT_MS;

        PendingConfirmation confirmation = new PendingConfirmation(token, expiresAt, onAccept, onDeny);
        pending.put(player.getUniqueId(), confirmation);

        PluginLang pluginLang = plugin.getPluginLang();
        Component yes = pluginLang.getPlain("confirm-button-yes")
                .clickEvent(ClickEvent.runCommand("/evet"))
                .hoverEvent(HoverEvent.showText(pluginLang.getPlain("confirm-button-yes-hover")));
        Component no = pluginLang.getPlain("confirm-button-no")
                .clickEvent(ClickEvent.runCommand("/hayir"))
                .hoverEvent(HoverEvent.showText(pluginLang.getPlain("confirm-button-no-hover")));
        Component cancelButton = pluginLang.getPlain("confirm-button-cancel")
                .clickEvent(ClickEvent.runCommand("/iptal"))
                .hoverEvent(HoverEvent.showText(pluginLang.getPlain("confirm-button-cancel-hover")));

        player.sendMessage(prompt);
        player.sendMessage(yes
                .append(Component.space())
                .append(no)
                .append(Component.space())
                .append(cancelButton));
    }

    public String getPendingToken(UUID playerId) {
        PendingConfirmation confirmation = pending.get(playerId);
        if (confirmation == null || confirmation.isExpired()) {
            return null;
        }
        return confirmation.token();
    }

    public boolean accept(Player player, String token) {
        PendingConfirmation confirmation = getValid(player, token);
        if (confirmation == null) {
            return false;
        }

        pending.remove(player.getUniqueId());
        confirmation.onAccept().run();
        plugin.getPluginLang().send(player, "confirm-accepted");
        return true;
    }

    public boolean deny(Player player, String token) {
        PendingConfirmation confirmation = getValid(player, token);
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

    private PendingConfirmation getValid(Player player, String token) {
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

        if (!confirmation.token().equals(token)) {
            plugin.getPluginLang().send(player, "confirm-invalid-token");
            return null;
        }

        return confirmation;
    }

    private String generateToken() {
        return Integer.toHexString(ThreadLocalRandom.current().nextInt(0x100000, 0xFFFFFF));
    }
}
