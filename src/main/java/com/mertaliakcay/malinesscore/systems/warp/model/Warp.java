package com.mertaliakcay.malinesscore.systems.warp.model;

import com.mertaliakcay.malinesscore.teleport.TeleportDestination;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public final class Warp implements TeleportDestination {

    private String name;
    private boolean enabled;
    private String description;
    private String worldName;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private long createdAt;
    private long updatedAt;

    public Warp(
            String name,
            boolean enabled,
            String description,
            String worldName,
            double x,
            double y,
            double z,
            float yaw,
            float pitch,
            long createdAt,
            long updatedAt
    ) {
        this.name = name;
        this.enabled = enabled;
        this.description = description == null ? "" : description;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Warp fromLocation(String name, Location location) {
        long now = System.currentTimeMillis();
        return new Warp(
                name,
                true,
                "",
                location.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch(),
                now,
                now
        );
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? "" : description;
    }

    @Override
    public String getWorldName() {
        return worldName;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void touchUpdated() {
        this.updatedAt = System.currentTimeMillis();
    }

    public void updateLocation(Location location) {
        this.worldName = location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
        touchUpdated();
    }

    @Override
    public Location toLocation() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        return new Location(world, x, y, z, yaw, pitch);
    }
}
