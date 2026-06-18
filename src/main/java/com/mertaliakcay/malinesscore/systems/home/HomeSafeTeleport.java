package com.mertaliakcay.malinesscore.systems.home;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public final class HomeSafeTeleport {

    private HomeSafeTeleport() {
    }

    public static boolean isSafe(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }

        World world = location.getWorld();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        Block feet = world.getBlockAt(x, y, z);
        Block head = world.getBlockAt(x, y + 1, z);
        Block ground = world.getBlockAt(x, y - 1, z);

        if (!feet.isPassable() || !head.isPassable()) {
            return false;
        }

        if (!ground.getType().isSolid()) {
            return false;
        }

        Material feetType = feet.getType();
        Material headType = head.getType();
        if (isDangerous(feetType) || isDangerous(headType) || isDangerous(ground.getType())) {
            return false;
        }

        return y >= world.getMinHeight() && y < world.getMaxHeight();
    }

    public static boolean isValidSetLocation(Player player, Location location) {
        if (location.getWorld() == null) {
            return false;
        }

        World world = location.getWorld();
        if (!world.getWorldBorder().isInside(location)) {
            return false;
        }

        int y = location.getBlockY();
        return y >= world.getMinHeight() && y < world.getMaxHeight();
    }

    private static boolean isDangerous(Material material) {
        return material == Material.LAVA
                || material == Material.FIRE
                || material == Material.SOUL_FIRE
                || material == Material.MAGMA_BLOCK
                || material == Material.CACTUS
                || material == Material.SWEET_BERRY_BUSH
                || material == Material.WITHER_ROSE
                || material == Material.CAMPFIRE
                || material == Material.SOUL_CAMPFIRE;
    }
}
