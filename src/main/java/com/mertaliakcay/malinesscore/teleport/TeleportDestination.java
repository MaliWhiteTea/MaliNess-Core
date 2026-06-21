package com.mertaliakcay.malinesscore.teleport;

import org.bukkit.Location;

public interface TeleportDestination {

    Location toLocation();

    String getWorldName();
}
