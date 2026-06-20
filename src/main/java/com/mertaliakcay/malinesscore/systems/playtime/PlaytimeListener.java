package com.mertaliakcay.malinesscore.systems.playtime;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlaytimeListener implements Listener {

    private final PlaytimeService playtimeService;

    public PlaytimeListener(PlaytimeService playtimeService) {
        this.playtimeService = playtimeService;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        playtimeService.startSession(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        playtimeService.endSession(event.getPlayer());
    }
}
