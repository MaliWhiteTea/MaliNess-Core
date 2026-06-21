package com.mertaliakcay.malinesscore.systems.pwarp.model;

import com.mertaliakcay.malinesscore.teleport.TeleportDestination;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

public final class Pwarp implements TeleportDestination {

    private String name;
    private UUID ownerId;
    private String ownerName;
    private String description;
    private String worldName;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private long createdAt;
    private long updatedAt;
    private long visitCount;
    private long lastVisitedAt;

    public Pwarp(
            String name,
            UUID ownerId,
            String ownerName,
            String description,
            String worldName,
            double x,
            double y,
            double z,
            float yaw,
            float pitch,
            long createdAt,
            long updatedAt,
            long visitCount,
            long lastVisitedAt
    ) {
        this.name = name;
        this.ownerId = ownerId;
        this.ownerName = ownerName == null ? "" : ownerName;
        this.description = description == null ? "" : description;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.visitCount = visitCount;
        this.lastVisitedAt = lastVisitedAt;
    }

    public static Pwarp fromLocation(String name, UUID ownerId, String ownerName, Location location) {
        long now = System.currentTimeMillis();
        return new Pwarp(
                name.trim(),
                ownerId,
                ownerName,
                "",
                location.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch(),
                now,
                now,
                0L,
                0L
        );
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName == null ? "" : ownerName;
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

    public long getVisitCount() {
        return visitCount;
    }

    public long getLastVisitedAt() {
        return lastVisitedAt;
    }

    public void touchUpdated() {
        this.updatedAt = System.currentTimeMillis();
    }

    public void recordVisit() {
        this.visitCount++;
        this.lastVisitedAt = System.currentTimeMillis();
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
