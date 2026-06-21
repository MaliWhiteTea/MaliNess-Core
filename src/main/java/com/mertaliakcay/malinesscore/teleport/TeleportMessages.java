package com.mertaliakcay.malinesscore.teleport;

import com.mertaliakcay.malinesscore.util.SystemLang;
import org.bukkit.entity.Player;

public interface TeleportMessages {

    void sendVehicleBlocked(Player player);

    void sendCountdown(Player player, int secondsLeft);

    void sendStarting(Player player);

    void sendCancelled(Player player);

    void sendFailed(Player player);

    void sendWorldNotLoaded(Player player);

    static TeleportMessages fromSystemLang(SystemLang lang) {
        return new TeleportMessages() {
            @Override
            public void sendVehicleBlocked(Player player) {
                lang.send(player, "teleport-vehicle-blocked");
            }

            @Override
            public void sendCountdown(Player player, int secondsLeft) {
                lang.send(player, "teleport-countdown", "seconds", secondsLeft);
            }

            @Override
            public void sendStarting(Player player) {
                lang.send(player, "teleport-starting");
            }

            @Override
            public void sendCancelled(Player player) {
                lang.send(player, "teleport-cancelled");
            }

            @Override
            public void sendFailed(Player player) {
                lang.send(player, "teleport-failed");
            }

            @Override
            public void sendWorldNotLoaded(Player player) {
                lang.send(player, "world-not-loaded");
            }
        };
    }
}
