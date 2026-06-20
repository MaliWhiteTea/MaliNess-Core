package com.mertaliakcay.malinesscore.systems.vanish;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.mertaliakcay.malinesscore.MaliNessCore;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class ProtocolLibVanishEnhancer implements VanishPacketEnhancer {

    private final MaliNessCore plugin;
    private final VanishService vanishService;
    private final boolean hideChestAnimation;
    private final boolean hideChestSound;
    private final boolean hideInteractionSound;
    private final VanishInteractionTracker interactionTracker = new VanishInteractionTracker();
    private PacketAdapter packetAdapter;

    public ProtocolLibVanishEnhancer(
            MaliNessCore plugin,
            VanishService vanishService,
            boolean hideChestAnimation,
            boolean hideChestSound,
            boolean hideInteractionSound
    ) {
        this.plugin = plugin;
        this.vanishService = vanishService;
        this.hideChestAnimation = hideChestAnimation;
        this.hideChestSound = hideChestSound;
        this.hideInteractionSound = hideInteractionSound;
    }

    @Override
    public void enable() {
        if (packetAdapter != null) {
            return;
        }

        packetAdapter = new PacketAdapter(
                plugin,
                ListenerPriority.NORMAL,
                PacketType.Play.Server.NAMED_SOUND_EFFECT,
                PacketType.Play.Server.WORLD_EVENT,
                PacketType.Play.Server.BLOCK_ACTION,
                PacketType.Play.Server.ENTITY_SOUND
        ) {
            @Override
            public void onPacketSending(PacketEvent event) {
                Player viewer = event.getPlayer();
                if (viewer == null) {
                    return;
                }

                if (shouldCancelEntitySound(event, viewer)) {
                    event.setCancelled(true);
                    return;
                }

                UUID ownerId = findInteractionOwner(event);
                if (ownerId == null) {
                    return;
                }

                Player owner = plugin.getServer().getPlayer(ownerId);
                if (owner == null || !vanishService.isVanished(owner)) {
                    interactionTracker.clear(ownerId);
                    return;
                }

                if (vanishService.canSee(viewer, owner)) {
                    return;
                }

                event.setCancelled(true);
            }
        };

        ProtocolLibrary.getProtocolManager().addPacketListener(packetAdapter);
    }

    @Override
    public void disable() {
        if (packetAdapter != null) {
            ProtocolLibrary.getProtocolManager().removePacketListener(packetAdapter);
            packetAdapter = null;
        }
        interactionTracker.clearAll();
    }

    @Override
    public void registerPlayer(Player player) {
    }

    @Override
    public void unregisterPlayer(Player player) {
        interactionTracker.clear(player.getUniqueId());
    }

    @Override
    public void trackInteraction(UUID playerId, Location location) {
        interactionTracker.track(playerId, location);
    }

    private boolean shouldCancelEntitySound(PacketEvent event, Player viewer) {
        if (!hideInteractionSound || event.getPacket().getType() != PacketType.Play.Server.ENTITY_SOUND) {
            return false;
        }

        Entity source = event.getPacket().getEntityModifier(event).readSafely(0);
        if (!(source instanceof Player owner) || !vanishService.isVanished(owner)) {
            return false;
        }

        return !vanishService.canSee(viewer, owner);
    }

    private UUID findInteractionOwner(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        Player viewer = event.getPlayer();
        String worldName = viewer.getWorld().getName();

        if (hideChestAnimation && packet.getType() == PacketType.Play.Server.WORLD_EVENT) {
            Integer effect = packet.getIntegers().readSafely(0);
            BlockPosition position = packet.getBlockPositionModifier().readSafely(0);
            if (effect != null && position != null && isChestWorldEvent(effect)) {
                UUID owner = interactionTracker.findOwnerNear(position.getX(), position.getY(), position.getZ(), worldName);
                if (owner != null) {
                    return owner;
                }
            }
        }

        if (hideChestAnimation && packet.getType() == PacketType.Play.Server.BLOCK_ACTION) {
            BlockPosition position = packet.getBlockPositionModifier().readSafely(0);
            Integer action = packet.getIntegers().readSafely(0);
            if (position != null && action != null && (action == 0 || action == 1)) {
                UUID owner = interactionTracker.findOwnerNear(position.getX(), position.getY(), position.getZ(), worldName);
                if (owner != null) {
                    return owner;
                }
            }
        }

        if ((hideChestSound || hideInteractionSound) && packet.getType() == PacketType.Play.Server.NAMED_SOUND_EFFECT) {
            BlockPosition position = packet.getBlockPositionModifier().readSafely(0);
            if (position != null) {
                UUID owner = interactionTracker.findOwnerNear(position.getX(), position.getY(), position.getZ(), worldName);
                if (owner != null) {
                    return owner;
                }
            }

            Float x = packet.getFloat().readSafely(0);
            Float y = packet.getFloat().readSafely(1);
            Float z = packet.getFloat().readSafely(2);
            if (x != null && y != null && z != null) {
                return interactionTracker.findOwnerNear(x.intValue(), y.intValue(), z.intValue(), worldName);
            }
        }

        return null;
    }

    private boolean isChestWorldEvent(int effect) {
        return effect == 1003
                || effect == 1004
                || effect == 1032
                || effect == 1033
                || effect == 2000;
    }
}
