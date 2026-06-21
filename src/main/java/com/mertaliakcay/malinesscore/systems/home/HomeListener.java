package com.mertaliakcay.malinesscore.systems.home;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public final class HomeListener implements Listener {

    private final HomeService homeService;

    public HomeListener(HomeService homeService) {
        this.homeService = homeService;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        homeService.cleanupPlayer(playerId);
    }
}
