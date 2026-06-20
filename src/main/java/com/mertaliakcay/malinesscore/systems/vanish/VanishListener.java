package com.mertaliakcay.malinesscore.systems.vanish;

import io.papermc.paper.event.entity.EntityInsideBlockEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Powerable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.GenericGameEvent;
import org.bukkit.inventory.InventoryHolder;

import java.util.Locale;
import java.util.Set;

public final class VanishListener implements Listener {

    private static final Set<String> PRIVATE_MESSAGE_COMMANDS = Set.of(
            "msg", "tell", "w", "whisper", "pm", "t"
    );

    private final VanishSystem system;
    private final VanishService vanishService;
    private final VanishPacketEnhancer packetEnhancer;

    public VanishListener(VanishSystem system, VanishService vanishService, VanishPacketEnhancer packetEnhancer) {
        this.system = system;
        this.vanishService = vanishService;
        this.packetEnhancer = packetEnhancer;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        vanishService.handleJoin(event.getPlayer());

        if (system.isJoinQuitHidden() && vanishService.isVanished(event.getPlayer())) {
            event.joinMessage(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        if (system.isJoinQuitHidden() && vanishService.isVanished(event.getPlayer())) {
            event.quitMessage(null);
        }

        vanishService.handleQuit(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        if (!system.isHideDeathMessages() || !vanishService.isVanished(event.getEntity())) {
            return;
        }

        event.deathMessage(null);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        if (!system.isHideAdvancementMessages() || !vanishService.isVanished(event.getPlayer())) {
            return;
        }

        event.message(null);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!system.isPreventDamage()) {
            return;
        }

        if (event.getEntity() instanceof Player player && vanishService.isVanished(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (!system.isPreventDealingDamage()) {
            return;
        }

        if (event.getDamager() instanceof Player player && vanishService.isVanished(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!system.isPreventItemPickup()) {
            return;
        }

        if (event.getEntity() instanceof Player player && vanishService.isVanished(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMobTarget(EntityTargetLivingEntityEvent event) {
        if (!system.isPreventMobTargeting()) {
            return;
        }

        if (event.getTarget() instanceof Player player && vanishService.isVanished(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGenericGameEvent(GenericGameEvent event) {
        if (!system.isHideSculkSensor() || !(event.getEntity() instanceof Player player)) {
            return;
        }

        if (vanishService.isVanished(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityInsideBlock(EntityInsideBlockEvent event) {
        if (!system.isHidePressurePlates() || !(event.getEntity() instanceof Player player)) {
            return;
        }

        if (!vanishService.isVanished(player)) {
            return;
        }

        if (isPressurePlate(event.getBlock().getType())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!system.isHidePressurePlates() || !vanishService.isVanished(event.getPlayer())) {
            return;
        }

        if (event.getTo() == null) {
            return;
        }

        depowerPressurePlateAt(event.getTo());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPrivateMessage(PlayerCommandPreprocessEvent event) {
        if (!system.isBlockPrivateMessages()) {
            return;
        }

        String raw = event.getMessage();
        if (raw.isEmpty() || raw.charAt(0) != '/') {
            return;
        }

        String withoutSlash = raw.substring(1);
        int spaceIndex = withoutSlash.indexOf(' ');
        String command = spaceIndex == -1 ? withoutSlash : withoutSlash.substring(0, spaceIndex);
        command = stripNamespace(command).toLowerCase(Locale.ROOT);

        if (!PRIVATE_MESSAGE_COMMANDS.contains(command)) {
            return;
        }

        String[] parts = withoutSlash.split("\\s+");
        if (parts.length < 2) {
            return;
        }

        Player target = event.getPlayer().getServer().getPlayerExact(parts[1]);
        if (target == null || vanishService.canSee(event.getPlayer(), target)) {
            return;
        }

        event.setCancelled(true);
        system.getLang().send(event.getPlayer(), "private-message-blocked", "player", parts[1]);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (!vanishService.isVanished(event.getPlayer())) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block != null && isTrackedInteractionBlock(block.getType())) {
            packetEnhancer.trackInteraction(event.getPlayer().getUniqueId(), block.getLocation());
            return;
        }

        if (event.getAction() == Action.PHYSICAL && block != null && isPressurePlate(block.getType())) {
            depowerPressurePlate(block);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player) || !vanishService.isVanished(player)) {
            return;
        }

        Location location = resolveInventoryLocation(event.getInventory().getHolder());
        if (location != null) {
            packetEnhancer.trackInteraction(player.getUniqueId(), location);
        }
    }

    private Location resolveInventoryLocation(InventoryHolder holder) {
        if (holder instanceof org.bukkit.block.BlockState blockState) {
            return blockState.getLocation();
        }

        if (holder instanceof org.bukkit.block.DoubleChest doubleChest) {
            return new Location(doubleChest.getWorld(), doubleChest.getX(), doubleChest.getY(), doubleChest.getZ());
        }

        return null;
    }

    private void depowerPressurePlateAt(Location location) {
        depowerPressurePlate(location.getBlock());
        depowerPressurePlate(location.getBlock().getRelative(BlockFace.DOWN));
    }

    private void depowerPressurePlate(Block block) {
        if (!isPressurePlate(block.getType())) {
            return;
        }

        if (block.getBlockData() instanceof Powerable powerable && powerable.isPowered()) {
            powerable.setPowered(false);
            block.setBlockData(powerable, false);
        }
    }

    private boolean isTrackedInteractionBlock(Material material) {
        String name = material.name();
        return isSilentContainer(material)
                || name.endsWith("_BUTTON")
                || name.endsWith("_LEVER")
                || material == Material.LEVER;
    }

    private boolean isSilentContainer(Material material) {
        String name = material.name();
        return name.endsWith("_CHEST")
                || name.endsWith("_TRAPPED_CHEST")
                || name.equals("BARREL")
                || name.endsWith("_SHULKER_BOX")
                || material == Material.ENDER_CHEST
                || name.equals("FURNACE")
                || name.equals("BLAST_FURNACE")
                || name.equals("SMOKER")
                || name.equals("DISPENSER")
                || name.equals("DROPPER")
                || name.equals("HOPPER");
    }

    private boolean isPressurePlate(Material material) {
        String name = material.name();
        return name.endsWith("_PRESSURE_PLATE")
                || material == Material.LIGHT_WEIGHTED_PRESSURE_PLATE
                || material == Material.HEAVY_WEIGHTED_PRESSURE_PLATE;
    }

    private String stripNamespace(String command) {
        int colonIndex = command.indexOf(':');
        if (colonIndex >= 0 && colonIndex + 1 < command.length()) {
            return command.substring(colonIndex + 1);
        }
        return command;
    }
}
