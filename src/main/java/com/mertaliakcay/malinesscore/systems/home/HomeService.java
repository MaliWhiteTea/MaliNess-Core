package com.mertaliakcay.malinesscore.systems.home;

import com.mertaliakcay.malinesscore.MaliNessCore;
import com.mertaliakcay.malinesscore.confirmation.ConfirmationService;
import com.mertaliakcay.malinesscore.systems.home.model.HomeLocation;
import com.mertaliakcay.malinesscore.systems.home.model.PlayerHomes;
import com.mertaliakcay.malinesscore.util.CommandSuggestions;
import com.mertaliakcay.malinesscore.util.SystemLang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class HomeService {

    private final MaliNessCore plugin;
    private final HomeSystem system;
    private final HomeStorage storage;
    private final HomeLimitService limitService;
    private final HomeNameValidator nameValidator;
    private final HomeLogger logger;
    private final HomeRateLimiter rateLimiter;
    private final HomeTeleportManager teleportManager;
    private final ConfirmationService confirmationService;

    private final Map<UUID, Long> homeCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Long> setHomeCooldowns = new ConcurrentHashMap<>();

    private List<String> blockedWorlds = List.of();
    private int homeCooldownSeconds = 10;
    private int setHomeCooldownSeconds = 3;
    private boolean safeTeleportEnabled = true;
    private boolean askOnUnsafe = true;

    public HomeService(
            MaliNessCore plugin,
            HomeSystem system,
            HomeStorage storage,
            HomeLimitService limitService,
            HomeNameValidator nameValidator,
            HomeLogger logger,
            HomeRateLimiter rateLimiter,
            HomeTeleportManager teleportManager,
            ConfirmationService confirmationService
    ) {
        this.plugin = plugin;
        this.system = system;
        this.storage = storage;
        this.limitService = limitService;
        this.nameValidator = nameValidator;
        this.logger = logger;
        this.rateLimiter = rateLimiter;
        this.teleportManager = teleportManager;
        this.confirmationService = confirmationService;
    }

    public void reloadFromConfig() {
        blockedWorlds = system.getConfig().get().getStringList("blocked-worlds").stream()
                .map(world -> world.toLowerCase(Locale.ROOT))
                .toList();
        homeCooldownSeconds = system.getConfig().get().getInt("teleport.cooldown-seconds", 10);
        setHomeCooldownSeconds = system.getConfig().get().getInt("teleport.sethome-cooldown-seconds", 3);
        safeTeleportEnabled = system.getConfig().get().getBoolean("safe-teleport.enabled", true);
        askOnUnsafe = system.getConfig().get().getBoolean("safe-teleport.ask-on-unsafe", true);
    }

    public void handleSetHome(CommandSender sender, String[] args) {
        SystemLang lang = system.getLang();
        if (!system.isEnabled()) {
            lang.send(sender, "system-disabled");
            return;
        }

        if (!(sender instanceof Player player)) {
            lang.send(sender, "console-not-supported");
            return;
        }

        if (!player.hasPermission(HomeSystem.PERM_SETHOME)) {
            lang.send(sender, "no-permission");
            return;
        }

        if (rateLimiter.isRateLimited(player)) {
            lang.send(sender, "rate-limited");
            return;
        }

        if (!bypassesTimers(player) && isOnCooldown(setHomeCooldowns, player, setHomeCooldownSeconds)) {
            lang.send(sender, "sethome-cooldown", "seconds", remainingCooldown(setHomeCooldowns, player, setHomeCooldownSeconds));
            return;
        }

        PlayerHomes homes = storage.load(player.getUniqueId());
        if (limitService.isOverLimit(player, homes)) {
            lang.send(sender, "over-limit-blocked",
                    "count", homes.size(),
                    "limit", limitService.getMaxHomes(player),
                    "homes", formatHomeList(homes));
            return;
        }

        String requestedName = args.length == 0 ? null : args[0];
        String homeName;

        if (requestedName == null || requestedName.isBlank()) {
            homeName = nameValidator.resolveDefaultName(homes::contains);
        } else {
            homeName = nameValidator.normalize(requestedName);
            if (!nameValidator.isValid(homeName)) {
                rateLimiter.recordFailure(player);
                lang.send(sender, "invalid-home-name", "name", requestedName);
                return;
            }
        }

        Location location = player.getLocation();
        if (isBlockedWorld(location.getWorld())) {
            lang.send(sender, "blocked-world");
            return;
        }

        if (!HomeSafeTeleport.isValidSetLocation(player, location)) {
            lang.send(sender, "invalid-set-location");
            return;
        }

        if (homes.contains(homeName)) {
            confirmationService.request(
                    player,
                    lang.get("confirm-overwrite", "home", homeName),
                    () -> saveHome(player, homes, homeName, location),
                    null
            );
            return;
        }

        if (!limitService.canCreateHome(player, homes)) {
            lang.send(sender, "home-limit-reached", "limit", limitService.getMaxHomes(player));
            return;
        }

        saveHome(player, homes, homeName, location);
    }

    public void handleHome(CommandSender sender, String[] args) {
        SystemLang lang = system.getLang();
        if (!system.isEnabled()) {
            lang.send(sender, "system-disabled");
            return;
        }

        if (args.length == 2) {
            handleAdminHome(sender, args[0], args[1]);
            return;
        }

        if (!(sender instanceof Player player)) {
            lang.send(sender, "console-not-supported");
            return;
        }

        if (!player.hasPermission(HomeSystem.PERM_USE)) {
            lang.send(sender, "no-permission");
            return;
        }

        if (rateLimiter.isRateLimited(player)) {
            lang.send(sender, "rate-limited");
            return;
        }

        PlayerHomes homes = homesOf(player);

        if (args.length == 0) {
            if (homes.size() == 1) {
                String onlyHome = homes.getHomeNames().iterator().next();
                teleportPlayerToHome(player, player, homes, onlyHome, false);
                return;
            }
            if (homes.size() > 1) {
                lang.send(player, "home-specify-name");
                sendHomeList(sender, player, homes, true, false);
                return;
            }
        }

        String homeName = args.length == 0 ? nameValidator.getDefaultName() : nameValidator.normalize(args[0]);
        teleportPlayerToHome(player, player, homes, homeName, false);
    }

    public void handleDelHome(CommandSender sender, String[] args) {
        SystemLang lang = system.getLang();
        if (!system.isEnabled()) {
            lang.send(sender, "system-disabled");
            return;
        }

        if (args.length == 2) {
            handleAdminDelete(sender, args[0], args[1]);
            return;
        }

        if (!(sender instanceof Player player)) {
            lang.send(sender, "console-delhome-usage");
            return;
        }

        if (!player.hasPermission(HomeSystem.PERM_DELHOME)) {
            lang.send(sender, "no-permission");
            return;
        }

        if (rateLimiter.isRateLimited(player)) {
            lang.send(sender, "rate-limited");
            return;
        }

        String homeName = args.length == 0 ? nameValidator.getDefaultName() : nameValidator.normalize(args[0]);
        PlayerHomes homes = storage.load(player.getUniqueId());

        if (!homes.contains(homeName)) {
            rateLimiter.recordFailure(player);
            lang.send(sender, "home-not-found", "home", homeName);
            return;
        }

        confirmationService.request(
                player,
                lang.get("confirm-delete", "home", homeName),
                () -> deleteHome(player, player, homes, homeName, false),
                null
        );
    }

    public void handleHomes(CommandSender sender, String[] args) {
        SystemLang lang = system.getLang();
        if (!system.isEnabled()) {
            lang.send(sender, "system-disabled");
            return;
        }

        if (args.length == 1) {
            if (!sender.hasPermission(HomeSystem.PERM_OTHERS_LIST)) {
                lang.send(sender, "no-permission-others");
                return;
            }

            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                if (sender instanceof Player player) {
                    rateLimiter.recordFailure(player);
                }
                lang.send(sender, "player-not-found", "player", args[0]);
                return;
            }

            sendHomeList(sender, target, storage.load(target.getUniqueId()), false,
                    sender.hasPermission(HomeSystem.PERM_OTHERS_TELEPORT));
            return;
        }

        if (!(sender instanceof Player player)) {
            lang.send(sender, "console-not-supported");
            return;
        }

        if (!player.hasPermission(HomeSystem.PERM_HOMES)) {
            lang.send(sender, "no-permission");
            return;
        }

        sendHomeList(sender, player, storage.load(player.getUniqueId()), true, false);
    }

    public void handleRenameHome(CommandSender sender, String[] args) {
        SystemLang lang = system.getLang();
        if (!system.isEnabled()) {
            lang.send(sender, "system-disabled");
            return;
        }

        if (!(sender instanceof Player player)) {
            lang.send(sender, "console-not-supported");
            return;
        }

        if (!player.hasPermission(HomeSystem.PERM_RENAME)) {
            lang.send(sender, "no-permission");
            return;
        }

        if (args.length != 2) {
            lang.send(sender, "usage-rename");
            return;
        }

        String oldName = nameValidator.normalize(args[0]);
        String newName = nameValidator.normalize(args[1]);

        if (!nameValidator.isValid(newName)) {
            rateLimiter.recordFailure(player);
            lang.send(player, "invalid-home-name", "name", args[1]);
            return;
        }

        PlayerHomes homes = storage.load(player.getUniqueId());
        if (!homes.contains(oldName)) {
            rateLimiter.recordFailure(player);
            lang.send(player, "home-not-found", "home", oldName);
            return;
        }

        if (oldName.equals(newName)) {
            lang.send(player, "rename-same-name");
            return;
        }

        if (homes.contains(newName)) {
            confirmationService.request(
                    player,
                    lang.get("confirm-overwrite-rename", "old", oldName, "new", newName),
                    () -> renameHome(player, homes, oldName, newName),
                    null
            );
            return;
        }

        renameHome(player, homes, oldName, newName);
    }

    public List<String> suggestSetHome(CommandSender sender, String[] args) {
        return List.of();
    }

    public List<String> suggestHome(CommandSender sender, String[] args) {
        if (args.length == 0) {
            return ownHomeSuggestions(sender);
        }
        if (args.length == 1) {
            return CommandSuggestions.filter(ownHomeSuggestions(sender), args[0]);
        }
        if (args.length == 2 && sender.hasPermission(HomeSystem.PERM_OTHERS_TELEPORT)) {
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                List<String> playerNames = new ArrayList<>();
                for (Player online : Bukkit.getOnlinePlayers()) {
                    playerNames.add(online.getName());
                }
                return CommandSuggestions.filter(playerNames, args[0]);
            }
            return CommandSuggestions.filter(new ArrayList<>(storage.load(target.getUniqueId()).getHomeNames()), args[1]);
        }
        return List.of();
    }

    public List<String> suggestDelHome(CommandSender sender, String[] args) {
        return suggestHome(sender, args);
    }

    public List<String> suggestHomes(CommandSender sender, String[] args) {
        if (args.length == 0) {
            if (sender.hasPermission(HomeSystem.PERM_OTHERS_LIST)) {
                return onlinePlayerNames();
            }
            return List.of();
        }
        if (args.length == 1 && sender.hasPermission(HomeSystem.PERM_OTHERS_LIST)) {
            return CommandSuggestions.filter(onlinePlayerNames(), args[0]);
        }
        return List.of();
    }

    public List<String> suggestRenameHome(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            return List.of();
        }

        PlayerHomes homes = storage.load(player.getUniqueId());
        List<String> names = new ArrayList<>(homes.getHomeNames());

        if (args.length == 0) {
            return CommandSuggestions.filter(names, "");
        }
        if (args.length == 1) {
            return CommandSuggestions.filter(names, args[0]);
        }
        return List.of();
    }

    private void handleAdminHome(CommandSender sender, String targetName, String homeName) {
        SystemLang lang = system.getLang();
        if (!sender.hasPermission(HomeSystem.PERM_OTHERS_TELEPORT)) {
            lang.send(sender, "no-permission-others");
            return;
        }

        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            if (sender instanceof Player player) {
                rateLimiter.recordFailure(player);
            }
            lang.send(sender, "player-not-found", "player", targetName);
            return;
        }

        PlayerHomes homes = storage.load(target.getUniqueId());
        HomeLocation home = homes.get(nameValidator.normalize(homeName));
        if (home == null) {
            if (sender instanceof Player player) {
                rateLimiter.recordFailure(player);
            }
            lang.send(sender, "home-not-found", "home", homeName);
            return;
        }

        if (!(sender instanceof Player admin)) {
            lang.send(sender, "console-not-supported");
            return;
        }

        Location location = home.toLocation();
        if (location == null || location.getWorld() == null) {
            lang.send(sender, "world-not-loaded");
            return;
        }

        teleportManager.teleportInstant(admin, home, () -> {
            logger.logAdmin(admin.getName(), target.getName(), target.getUniqueId().toString(), "TELEPORT", homeName, home);
            lang.send(sender, "admin-teleported", "player", target.getName(), "home", homeName);
        });
    }

    private void handleAdminDelete(CommandSender sender, String targetName, String homeName) {
        SystemLang lang = system.getLang();
        if (!sender.hasPermission(HomeSystem.PERM_OTHERS_DELETE)) {
            lang.send(sender, "no-permission-others");
            return;
        }

        OfflinePlayer target = resolvePlayer(targetName);
        if (target == null) {
            if (sender instanceof Player player) {
                rateLimiter.recordFailure(player);
            }
            lang.send(sender, "player-not-found", "player", targetName);
            return;
        }

        String normalizedHome = nameValidator.normalize(homeName);
        PlayerHomes homes = storage.load(target.getUniqueId());
        if (!homes.contains(normalizedHome)) {
            if (sender instanceof Player player) {
                rateLimiter.recordFailure(player);
            }
            lang.send(sender, "home-not-found", "home", homeName);
            return;
        }

        if (sender instanceof Player admin) {
            confirmationService.request(
                    admin,
                    lang.get("confirm-delete-other", "player", target.getName(), "home", normalizedHome),
                    () -> deleteHome(admin, target, homes, normalizedHome, true),
                    null
            );
            return;
        }

        deleteHomeConsole(sender, target, homes, normalizedHome);
    }

    private void deleteHomeConsole(CommandSender sender, OfflinePlayer owner, PlayerHomes homes, String homeName) {
        HomeLocation removed = homes.remove(homeName);
        if (removed == null) {
            system.getLang().send(sender, "home-not-found", "home", homeName);
            return;
        }

        storage.saveAsync(owner.getUniqueId(), homes);
        logger.logAdmin("CONSOLE", owner.getName(), owner.getUniqueId().toString(), "DELETE", homeName, removed);
        system.getLang().send(sender, "admin-deleted", "player", owner.getName(), "home", homeName);

        Player online = owner.getPlayer();
        if (online != null && online.isOnline()) {
            system.getLang().send(online, "home-deleted-by-admin", "home", homeName);
        }
    }

    private void teleportPlayerToHome(Player traveler, Player owner, PlayerHomes homes, String homeName, boolean adminTeleport) {
        SystemLang lang = system.getLang();

        if (!adminTeleport && limitService.isOverLimit(owner, homes)) {
            lang.send(traveler, "over-limit-blocked",
                    "count", homes.size(),
                    "limit", limitService.getMaxHomes(owner),
                    "homes", formatHomeList(homes));
            return;
        }

        HomeLocation home = homes.get(homeName);
        if (home == null) {
            rateLimiter.recordFailure(traveler);
            lang.send(traveler, "home-not-found", "home", homeName);
            return;
        }

        if (!adminTeleport && !bypassesTimers(traveler) && isOnCooldown(homeCooldowns, traveler, homeCooldownSeconds)) {
            lang.send(traveler, "home-cooldown", "seconds", remainingCooldown(homeCooldowns, traveler, homeCooldownSeconds));
            return;
        }

        Location location = home.toLocation();
        if (location == null || location.getWorld() == null) {
            rateLimiter.recordFailure(traveler);
            lang.send(traveler, "world-not-loaded");
            return;
        }

        Runnable onSuccess = () -> {
            homeCooldowns.put(traveler.getUniqueId(), System.currentTimeMillis());
            rateLimiter.reset(traveler);
            logger.logPlayer(owner.getName(), owner.getUniqueId().toString(), "TELEPORT", homeName, home);
            lang.send(traveler, "teleport-success", "home", homeName);
        };

        if (safeTeleportEnabled && !HomeSafeTeleport.isSafe(location)) {
            if (askOnUnsafe) {
                confirmationService.request(
                        traveler,
                        lang.get("confirm-unsafe-teleport", "home", homeName),
                        () -> startTeleport(traveler, home, onSuccess),
                        null
                );
                return;
            }

            lang.send(traveler, "unsafe-home-blocked", "home", homeName);
            return;
        }

        startTeleport(traveler, home, onSuccess);
    }

    private void startTeleport(Player player, HomeLocation home, Runnable onSuccess) {
        if (bypassesTimers(player)) {
            teleportManager.teleportInstant(player, home, onSuccess);
            return;
        }

        if (teleportManager.hasWarmup(player.getUniqueId())) {
            system.getLang().send(player, "teleport-already-pending");
            return;
        }

        system.getLang().send(player, "teleport-started", "seconds", system.getConfig().get().getInt("teleport.warmup-seconds", 5));
        teleportManager.startWarmup(player, home, onSuccess);
    }

    private void saveHome(Player player, PlayerHomes homes, String homeName, Location location) {
        boolean overwrite = homes.contains(homeName);
        if (!overwrite && !limitService.canCreateHome(player, homes)) {
            system.getLang().send(player, "home-limit-reached", "limit", limitService.getMaxHomes(player));
            return;
        }

        HomeLocation homeLocation = HomeLocation.fromLocation(location);
        homes.put(homeName, homeLocation);
        storage.saveAsync(player.getUniqueId(), homes);
        setHomeCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        rateLimiter.reset(player);

        logger.logPlayer(player.getName(), player.getUniqueId().toString(), overwrite ? "SET_OVERWRITE" : "SET", homeName, homeLocation);
        system.getLang().send(player, overwrite ? "home-overwritten" : "home-set", "home", homeName);
    }

    private void deleteHome(Player actor, OfflinePlayer owner, PlayerHomes homes, String homeName, boolean adminAction) {
        HomeLocation removed = homes.remove(homeName);
        if (removed == null) {
            system.getLang().send(actor, "home-not-found", "home", homeName);
            return;
        }

        storage.saveAsync(owner.getUniqueId(), homes);
        rateLimiter.reset(actor);

        if (adminAction) {
            logger.logAdmin(actor.getName(), owner.getName(), owner.getUniqueId().toString(), "DELETE", homeName, removed);
            system.getLang().send(actor, "admin-deleted", "player", owner.getName(), "home", homeName);
            Player online = owner.getPlayer();
            if (online != null && online.isOnline() && !actor.equals(online)) {
                system.getLang().send(online, "home-deleted-by-admin", "home", homeName);
            }
        } else {
            logger.logPlayer(owner.getName(), owner.getUniqueId().toString(), "DELETE", homeName, removed);
            system.getLang().send(actor, "home-deleted", "home", homeName);
        }
    }

    private void renameHome(Player player, PlayerHomes homes, String oldName, String newName) {
        HomeLocation location = homes.remove(oldName);
        if (location == null) {
            system.getLang().send(player, "home-not-found", "home", oldName);
            return;
        }

        homes.put(newName, location);
        storage.saveAsync(player.getUniqueId(), homes);
        logger.logPlayerRename(player.getName(), player.getUniqueId().toString(), oldName, newName);
        system.getLang().send(player, "home-renamed", "old", oldName, "new", newName);
    }

    private void sendHomeList(CommandSender sender, Player owner, PlayerHomes homes, boolean clickable, boolean adminTeleportClickable) {
        SystemLang lang = system.getLang();
        int limit = limitService.getMaxHomes(owner);

        if (homes.size() == 0) {
            lang.send(sender, "homes-empty");
            return;
        }

        lang.send(sender, "homes-header", "count", homes.size(), "limit", limit);

        for (Map.Entry<String, HomeLocation> entry : homes.getHomes().entrySet()) {
            HomeLocation location = entry.getValue();
            Component line = lang.get(
                    "homes-entry",
                    "home", entry.getKey(),
                    "world", location.getWorldName(),
                    "x", (int) location.getX(),
                    "y", (int) location.getY(),
                    "z", (int) location.getZ()
            );

            if (clickable && sender instanceof Player) {
                line = line.clickEvent(ClickEvent.runCommand("/home " + entry.getKey()))
                        .hoverEvent(HoverEvent.showText(lang.get("homes-entry-hover", "home", entry.getKey())));
            } else if (adminTeleportClickable && sender instanceof Player) {
                line = line.clickEvent(ClickEvent.runCommand("/home " + owner.getName() + " " + entry.getKey()))
                        .hoverEvent(HoverEvent.showText(lang.get("homes-entry-admin-hover", "player", owner.getName(), "home", entry.getKey())));
            }

            sender.sendMessage(line);
        }

        if (limitService.isOverLimit(owner, homes)) {
            lang.send(sender, "over-limit-warning",
                    "count", homes.size(),
                    "limit", limit,
                    "homes", formatHomeList(homes));
        }
    }

    private boolean bypassesTimers(Player player) {
        return player.isOp() || player.hasPermission(HomeSystem.PERM_BYPASS_TIME);
    }

    private OfflinePlayer resolvePlayer(String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) {
            return online;
        }

        OfflinePlayer offline = Bukkit.getOfflinePlayer(name);
        if (offline.hasPlayedBefore() || offline.isOnline()) {
            return offline;
        }

        return null;
    }

    private PlayerHomes homesOf(Player player) {
        return storage.load(player.getUniqueId());
    }

    private List<String> ownHomeSuggestions(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            return List.of();
        }

        List<String> suggestions = new ArrayList<>(storage.load(player.getUniqueId()).getHomeNames());
        if (sender.hasPermission(HomeSystem.PERM_OTHERS_TELEPORT)) {
            suggestions.addAll(onlinePlayerNames());
        }
        return suggestions;
    }

    private List<String> onlinePlayerNames() {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }

    private boolean isBlockedWorld(World world) {
        return world != null && blockedWorlds.contains(world.getName().toLowerCase(Locale.ROOT));
    }

    private boolean isOnCooldown(Map<UUID, Long> map, Player player, int seconds) {
        if (bypassesTimers(player)) {
            return false;
        }
        Long last = map.get(player.getUniqueId());
        return last != null && System.currentTimeMillis() - last < seconds * 1000L;
    }

    private int remainingCooldown(Map<UUID, Long> map, Player player, int seconds) {
        Long last = map.get(player.getUniqueId());
        if (last == null) {
            return 0;
        }
        long remaining = seconds * 1000L - (System.currentTimeMillis() - last);
        return (int) Math.max(1, Math.ceil(remaining / 1000.0));
    }

    private String formatHomeList(PlayerHomes homes) {
        return String.join(", ", homes.getHomeNames());
    }
}
