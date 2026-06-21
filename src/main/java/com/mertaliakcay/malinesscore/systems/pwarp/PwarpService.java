package com.mertaliakcay.malinesscore.systems.pwarp;

import com.mertaliakcay.malinesscore.MaliNessCore;
import com.mertaliakcay.malinesscore.confirmation.ConfirmationService;
import com.mertaliakcay.malinesscore.systems.AbstractGameSystem;
import com.mertaliakcay.malinesscore.systems.pwarp.model.Pwarp;
import com.mertaliakcay.malinesscore.systems.warp.WarpSystem;
import com.mertaliakcay.malinesscore.teleport.SafeTeleport;
import com.mertaliakcay.malinesscore.teleport.TeleportMessages;
import com.mertaliakcay.malinesscore.teleport.TeleportService;
import com.mertaliakcay.malinesscore.teleport.TeleportWarmupMessages;
import com.mertaliakcay.malinesscore.teleport.WarmupType;
import com.mertaliakcay.malinesscore.util.CommandSuggestions;
import com.mertaliakcay.malinesscore.util.MaliNessColorUtil;
import com.mertaliakcay.malinesscore.util.SystemLang;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class PwarpService {

    private static final String CONFIRM_FLAG = "--confirm";

    private final MaliNessCore plugin;
    private final PwarpSystem system;
    private final PwarpStorage storage;
    private final PwarpNameValidator nameValidator;
    private final PwarpLimitService limitService;
    private final PwarpRateLimiter rateLimiter;
    private final PwarpLogger logger;
    private final TeleportService teleportService;
    private final ConfirmationService confirmationService;

    private final Map<UUID, Long> teleportCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Long> setCooldowns = new ConcurrentHashMap<>();

    private List<String> blockedWorlds = List.of();
    private int warmupSeconds = 5;
    private int cooldownSeconds = 5;
    private int setCooldownSeconds = 3;
    private int fireResistanceSeconds = 3;
    private boolean safeTeleportEnabled = true;
    private boolean askOnUnsafe = true;
    private boolean blockPwarpIfAdminWarpExists = true;
    private String dateTimeFormat = "dd.MM.yyyy HH:mm";
    private String neverVisitedText = "Hiç ziyaret edilmedi";

    public PwarpService(
            MaliNessCore plugin,
            PwarpSystem system,
            PwarpStorage storage,
            PwarpNameValidator nameValidator,
            PwarpLimitService limitService,
            PwarpRateLimiter rateLimiter,
            PwarpLogger logger,
            TeleportService teleportService,
            ConfirmationService confirmationService
    ) {
        this.plugin = plugin;
        this.system = system;
        this.storage = storage;
        this.nameValidator = nameValidator;
        this.limitService = limitService;
        this.rateLimiter = rateLimiter;
        this.logger = logger;
        this.teleportService = teleportService;
        this.confirmationService = confirmationService;
    }

    public SystemLang getLang() {
        return system.getLang();
    }

    public void reloadFromConfig() {
        var configuration = system.getConfig().get();
        blockedWorlds = configuration.getStringList("blocked-worlds").stream()
                .map(world -> world.toLowerCase(Locale.ROOT))
                .toList();
        warmupSeconds = configuration.getInt("teleport.warmup-seconds", 5);
        cooldownSeconds = configuration.getInt("teleport.cooldown-seconds", 5);
        setCooldownSeconds = configuration.getInt("teleport.set-cooldown-seconds", 3);
        fireResistanceSeconds = configuration.getInt("teleport.fire-resistance-seconds", 3);
        safeTeleportEnabled = configuration.getBoolean("safe-teleport.enabled", true);
        askOnUnsafe = configuration.getBoolean("safe-teleport.ask-on-unsafe", true);
        blockPwarpIfAdminWarpExists = configuration.getBoolean(
                "name-collision.block-pwarp-if-admin-warp-exists",
                true
        );
        dateTimeFormat = configuration.getString("display.datetime-format", "dd.MM.yyyy HH:mm");
        neverVisitedText = configuration.getString("display.never-visited", "Hiç ziyaret edilmedi");
    }

    public void handle(CommandSender sender, String[] args) {
        if (!system.isEnabled()) {
            system.getLang().send(sender, "system-disabled");
            return;
        }

        if (args.length == 0) {
            sendList(sender, 1);
            return;
        }

        if (isListKeyword(args[0])) {
            handleList(sender, java.util.Arrays.copyOfRange(args, 1, args.length));
            return;
        }

        if (isSetKeyword(args[0])) {
            if (args.length != 2) {
                system.getLang().send(sender, "usage-set");
                return;
            }
            handleCreate(sender, args[1]);
            return;
        }

        if (isRemoveKeyword(args[0])) {
            ParsedArgs parsed = parseArgs(java.util.Arrays.copyOfRange(args, 1, args.length));
            if (parsed.args.length == 1) {
                handleDeleteOwn(sender, parsed.args[0], parsed.confirmed);
                return;
            }
            if (parsed.args.length == 2) {
                handleAdminDelete(sender, parsed.args[0], parsed.args[1], parsed.confirmed);
                return;
            }
            system.getLang().send(sender, "usage-remove");
            return;
        }

        if (isEditKeyword(args[0])) {
            handleEdit(sender, args);
            return;
        }

        if (args.length == 1 && args[0].matches("\\d+")) {
            sendList(sender, Integer.parseInt(args[0]));
            return;
        }

        if (args.length == 1) {
            handleTeleport(sender, args[0]);
            return;
        }

        system.getLang().send(sender, "usage");
    }

    public List<String> suggest(CommandSender sender, String[] args) {
        if (!system.isEnabled()) {
            return List.of();
        }

        if (args.length == 0) {
            List<String> suggestions = new ArrayList<>();
            if (sender.hasPermission(PwarpSystem.PERM_SET)) {
                suggestions.addAll(PwarpSystem.SET_ALIASES);
            }
            if (sender.hasPermission(PwarpSystem.PERM_DELETE)) {
                suggestions.addAll(PwarpSystem.REMOVE_ALIASES);
            }
            if (sender.hasPermission(PwarpSystem.PERM_EDIT)) {
                suggestions.addAll(PwarpSystem.EDIT_ALIASES);
            }
            if (sender.hasPermission(PwarpSystem.PERM_LIST)) {
                suggestions.addAll(PwarpSystem.LIST_ALIASES);
            }
            suggestions.addAll(suggestPwarpNames(""));
            return CommandSuggestions.filter(suggestions, "");
        }

        if (args.length == 1) {
            if (isManageKeywordPartial(args[0])) {
                List<String> manage = new ArrayList<>();
                if (sender.hasPermission(PwarpSystem.PERM_SET)) {
                    manage.addAll(PwarpSystem.SET_ALIASES);
                }
                if (sender.hasPermission(PwarpSystem.PERM_DELETE)) {
                    manage.addAll(PwarpSystem.REMOVE_ALIASES);
                }
                if (sender.hasPermission(PwarpSystem.PERM_EDIT)) {
                    manage.addAll(PwarpSystem.EDIT_ALIASES);
                }
                if (sender.hasPermission(PwarpSystem.PERM_LIST)) {
                    manage.addAll(PwarpSystem.LIST_ALIASES);
                }
                return CommandSuggestions.filter(manage, args[0]);
            }

            if (args[0].matches("\\d*")) {
                List<String> merged = new ArrayList<>(List.of("1", "2", "3"));
                merged.addAll(suggestPwarpNames(args[0]));
                return CommandSuggestions.filter(merged, args[0]);
            }

            return CommandSuggestions.filter(suggestPwarpNames(args[0]), args[0]);
        }

        if (args.length == 2 && isSetKeyword(args[0])) {
            return CommandSuggestions.filter(List.of(), args[1]);
        }

        if (args.length == 2 && isRemoveKeyword(args[0])) {
            List<String> suggestions = new ArrayList<>();
            if (sender instanceof Player player) {
                suggestions.addAll(suggestOwnPwarpNames(player, args[1]));
            }
            if (sender.hasPermission(PwarpSystem.PERM_MANAGE)) {
                suggestions.addAll(onlinePlayerNames());
            }
            return CommandSuggestions.filter(suggestions, args[1]);
        }

        if (args.length == 3 && isRemoveKeyword(args[0]) && sender.hasPermission(PwarpSystem.PERM_MANAGE)) {
            OfflinePlayer target = resolvePlayer(args[1]);
            if (target == null) {
                return List.of();
            }
            return CommandSuggestions.filter(suggestOwnerPwarpNames(target.getUniqueId(), args[2]), args[2]);
        }

        if (args.length == 2 && isEditKeyword(args[0]) && sender instanceof Player player) {
            return CommandSuggestions.filter(suggestOwnPwarpNames(player, args[1]), args[1]);
        }

        if (args.length == 3 && isEditKeyword(args[0]) && sender instanceof Player player) {
            Pwarp pwarp = storage.find(args[1]);
            if (pwarp == null || !pwarp.getOwnerId().equals(player.getUniqueId())) {
                return List.of();
            }

            List<String> editOptions = new ArrayList<>(PwarpSystem.EDIT_ACTIONS);
            if (!isEditAction(args[2])) {
                editOptions.addAll(suggestPwarpNames(args[2]));
            }
            return CommandSuggestions.filter(editOptions, args[2]);
        }

        return List.of();
    }

    public Collection<Pwarp> getAllPwarps() {
        return storage.getAll();
    }

    public String formatCreatedAt(Pwarp pwarp) {
        return formatTimestamp(pwarp.getCreatedAt());
    }

    public String formatLastVisit(Pwarp pwarp) {
        return formatTimestamp(pwarp.getLastVisitedAt());
    }

    public void sendList(CommandSender sender, int page) {
        if (!sender.hasPermission(PwarpSystem.PERM_LIST)) {
            system.getLang().send(sender, "no-permission");
            return;
        }

        PwarpListHelp.send(plugin, this, sender, page);
    }

    public void handleList(CommandSender sender, String[] args) {
        if (!system.isEnabled()) {
            system.getLang().send(sender, "system-disabled");
            return;
        }

        int page = 1;
        if (args.length >= 1) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException exception) {
                system.getLang().send(sender, "usage-list");
                return;
            }
        }

        sendList(sender, page);
    }

    private void handleTeleport(CommandSender sender, String rawName) {
        SystemLang lang = system.getLang();
        if (!(sender instanceof Player player)) {
            lang.send(sender, "console-teleport-not-supported");
            return;
        }

        if (!sender.hasPermission(PwarpSystem.PERM_USE)) {
            lang.send(sender, "no-permission");
            return;
        }

        if (rateLimiter.isRateLimited(player)) {
            lang.send(sender, "rate-limited");
            return;
        }

        Pwarp pwarp = storage.find(rawName);
        if (pwarp == null) {
            rateLimiter.recordFailure(player);
            lang.send(sender, "pwarp-not-found", "pwarp", rawName);
            return;
        }

        teleportToPwarp(player, pwarp);
    }

    private void teleportToPwarp(Player player, Pwarp pwarp) {
        SystemLang lang = system.getLang();

        Location location = pwarp.toLocation();
        if (location == null || location.getWorld() == null) {
            rateLimiter.recordFailure(player);
            lang.send(player, "world-not-loaded");
            storage.validateWorlds();
            return;
        }

        Runnable onSuccess = () -> {
            teleportCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
            if (!player.getUniqueId().equals(pwarp.getOwnerId())) {
                pwarp.recordVisit();
                storage.put(pwarp);
            }
            rateLimiter.reset(player);
            logger.logTeleport(player.getName(), player.getUniqueId().toString(), pwarp);
            lang.send(player, "teleport-success", "pwarp", pwarp.getName());
        };

        if (safeTeleportEnabled && !SafeTeleport.isSafe(location)) {
            if (askOnUnsafe) {
                confirmationService.request(
                        player,
                        lang.get("confirm-unsafe-teleport", "pwarp", pwarp.getName()),
                        () -> startTeleport(player, pwarp, onSuccess),
                        null
                );
                return;
            }

            lang.send(player, "unsafe-pwarp-blocked", "pwarp", pwarp.getName());
            return;
        }

        startTeleport(player, pwarp, onSuccess);
    }

    private void startTeleport(Player player, Pwarp pwarp, Runnable onSuccess) {
        SystemLang lang = system.getLang();
        TeleportMessages messages = TeleportMessages.fromSystemLang(lang);

        if (!bypassesTimers(player) && isOnCooldown(player)) {
            lang.send(player, "pwarp-cooldown", "seconds", remainingCooldown(player));
            return;
        }

        if (player.isInsideVehicle() && !PwarpSystem.bypassesPwarpRestrictions(player)) {
            lang.send(player, "teleport-vehicle-blocked");
            return;
        }

        if (blockIfWarmupPending(player, lang)) {
            return;
        }

        if (bypassesTimers(player)) {
            teleportService.teleportInstant(player, pwarp, messages, fireResistanceSeconds, onSuccess);
            return;
        }

        lang.send(player, "teleport-started", "pwarp", pwarp.getName(), "seconds", warmupSeconds);
        teleportService.startWarmup(
                player,
                pwarp,
                messages,
                warmupSeconds,
                fireResistanceSeconds,
                PwarpSystem::bypassesPwarpRestrictions,
                WarmupType.PWARP,
                onSuccess
        );
    }

    private boolean blockIfWarmupPending(Player player, SystemLang lang) {
        if (!teleportService.hasWarmup(player.getUniqueId())) {
            return false;
        }

        WarmupType type = teleportService.getWarmupType(player.getUniqueId());
        TeleportWarmupMessages.sendBlocked(player, lang, type, WarmupType.PWARP);
        return true;
    }

    private void handleCreate(CommandSender sender, String rawName) {
        SystemLang lang = system.getLang();
        if (!(sender instanceof Player player)) {
            lang.send(sender, "console-set-not-supported");
            return;
        }

        if (!sender.hasPermission(PwarpSystem.PERM_SET)) {
            lang.send(sender, "no-permission-set");
            return;
        }

        if (rateLimiter.isRateLimited(player)) {
            lang.send(sender, "rate-limited");
            return;
        }

        if (!bypassesTimers(player) && isOnSetCooldown(player)) {
            lang.send(sender, "set-cooldown", "seconds", remainingSetCooldown(player));
            return;
        }

        if (!nameValidator.isValid(rawName)) {
            rateLimiter.recordFailure(player);
            lang.send(sender, "invalid-pwarp-name", "pwarp", rawName);
            return;
        }

        if (storage.contains(rawName)) {
            rateLimiter.recordFailure(player);
            lang.send(sender, "pwarp-already-exists", "pwarp", rawName);
            return;
        }

        if (blockPwarpIfAdminWarpExists && isAdminWarpNameTaken(rawName)) {
            rateLimiter.recordFailure(player);
            lang.send(sender, "admin-warp-name-collision", "pwarp", rawName);
            return;
        }

        int currentCount = storage.countByOwner(player.getUniqueId());
        if (!limitService.canCreate(player, currentCount)) {
            lang.send(sender, "pwarp-limit-reached", "limit", limitService.getMaxPwarps(player));
            return;
        }

        Location location = player.getLocation();
        if (!SafeTeleport.isValidSetLocation(player, location)) {
            lang.send(sender, "invalid-set-location");
            return;
        }

        if (isBlockedWorld(location)) {
            lang.send(sender, "blocked-world", "world", location.getWorld().getName());
            return;
        }

        Pwarp pwarp = Pwarp.fromLocation(rawName.trim(), player.getUniqueId(), player.getName(), location);
        storage.put(pwarp);
        setCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        rateLimiter.reset(player);
        logger.logPlayer(player.getName(), player.getUniqueId().toString(), "CREATE", pwarp);
        lang.send(sender, "created", "pwarp", pwarp.getName());
    }

    private void handleDeleteOwn(CommandSender sender, String rawName, boolean confirmed) {
        SystemLang lang = system.getLang();
        if (!(sender instanceof Player player)) {
            lang.send(sender, "console-delete-not-supported");
            return;
        }

        if (!sender.hasPermission(PwarpSystem.PERM_DELETE)) {
            lang.send(sender, "no-permission-delete");
            return;
        }

        if (rateLimiter.isRateLimited(player)) {
            lang.send(sender, "rate-limited");
            return;
        }

        Pwarp pwarp = storage.find(rawName);
        if (pwarp == null) {
            rateLimiter.recordFailure(player);
            lang.send(sender, "pwarp-not-found", "pwarp", rawName);
            return;
        }

        if (!pwarp.getOwnerId().equals(player.getUniqueId())) {
            rateLimiter.recordFailure(player);
            lang.send(sender, "not-owner", "pwarp", pwarp.getName());
            return;
        }

        if (!confirmed) {
            confirmationService.request(
                    player,
                    lang.get("confirm-delete", "pwarp", pwarp.getName()),
                    () -> handleDeleteOwn(sender, pwarp.getName(), true),
                    null
            );
            return;
        }

        storage.remove(pwarp.getName());
        rateLimiter.reset(player);
        logger.logPlayer(player.getName(), player.getUniqueId().toString(), "DELETE", pwarp);
        lang.send(sender, "deleted", "pwarp", pwarp.getName());
    }

    private void handleAdminDelete(CommandSender sender, String targetName, String rawName, boolean confirmed) {
        SystemLang lang = system.getLang();
        if (!sender.hasPermission(PwarpSystem.PERM_MANAGE)) {
            lang.send(sender, "no-permission-manage");
            return;
        }

        if (sender instanceof Player player && rateLimiter.isRateLimited(player)) {
            lang.send(sender, "rate-limited");
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

        Pwarp pwarp = storage.find(rawName);
        if (pwarp == null || !pwarp.getOwnerId().equals(target.getUniqueId())) {
            if (sender instanceof Player player) {
                rateLimiter.recordFailure(player);
            }
            lang.send(sender, "pwarp-not-found", "pwarp", rawName);
            return;
        }

        if (!confirmed) {
            if (sender instanceof Player admin) {
                confirmationService.request(
                        admin,
                        lang.get(
                                "confirm-delete-other",
                                "player", resolveDisplayName(target, targetName),
                                "pwarp", pwarp.getName()
                        ),
                        () -> handleAdminDelete(sender, targetName, pwarp.getName(), true),
                        null
                );
                return;
            }

            lang.send(sender, "console-confirm-required");
            return;
        }

        storage.remove(pwarp.getName());
        if (sender instanceof Player admin) {
            rateLimiter.reset(admin);
        }
        logger.logAdmin(resolveActorName(sender), "DELETE", pwarp);
        lang.send(sender, "admin-deleted", "player", resolveDisplayName(target, targetName), "pwarp", pwarp.getName());

        Player online = target.getPlayer();
        if (online != null && online.isOnline() && sender instanceof Player admin && !admin.equals(online)) {
            lang.send(online, "pwarp-deleted-by-admin", "pwarp", pwarp.getName());
        }
    }

    private void handleEdit(CommandSender sender, String[] args) {
        SystemLang lang = system.getLang();
        if (!(sender instanceof Player player)) {
            lang.send(sender, "console-not-supported");
            return;
        }

        if (!sender.hasPermission(PwarpSystem.PERM_EDIT)) {
            lang.send(sender, "no-permission-edit");
            return;
        }

        if (args.length < 3) {
            lang.send(sender, "usage-edit");
            return;
        }

        Pwarp pwarp = storage.find(args[1]);
        if (pwarp == null) {
            rateLimiter.recordFailure(player);
            lang.send(sender, "pwarp-not-found", "pwarp", args[1]);
            return;
        }

        if (!pwarp.getOwnerId().equals(player.getUniqueId())) {
            rateLimiter.recordFailure(player);
            lang.send(sender, "not-owner", "pwarp", pwarp.getName());
            return;
        }

        String action = args[2].toLowerCase(Locale.ROOT);

        if (PwarpSystem.LOCATION_ACTIONS.contains(action)) {
            Location location = player.getLocation();
            if (!SafeTeleport.isValidSetLocation(player, location)) {
                lang.send(sender, "invalid-set-location");
                return;
            }

            if (isBlockedWorld(location)) {
                lang.send(sender, "blocked-world", "world", location.getWorld().getName());
                return;
            }

            pwarp.updateLocation(location);
            storage.put(pwarp);
            logger.logPlayer(player.getName(), player.getUniqueId().toString(), "RELOCATE", pwarp);
            lang.send(sender, "relocated", "pwarp", pwarp.getName());
            return;
        }

        if (PwarpSystem.DESCRIPTION_ACTIONS.contains(action)) {
            String description = args.length <= 3
                    ? ""
                    : String.join(" ", java.util.Arrays.copyOfRange(args, 3, args.length));
            description = MaliNessColorUtil.apply(description, player, plugin);
            pwarp.setDescription(description);
            pwarp.touchUpdated();
            storage.put(pwarp);
            logger.logPlayer(player.getName(), player.getUniqueId().toString(), "DESCRIPTION", pwarp);
            if (description.isBlank()) {
                lang.send(sender, "description-cleared", "pwarp", pwarp.getName());
            } else {
                lang.send(sender, "description-updated", "pwarp", pwarp.getName());
            }
            return;
        }

        if (args.length == 3 && !isEditAction(action)) {
            handleRename(sender, pwarp, args[2]);
            return;
        }

        lang.send(sender, "usage-edit");
    }

    private void handleRename(CommandSender sender, Pwarp pwarp, String newRawName) {
        SystemLang lang = system.getLang();
        if (!(sender instanceof Player player)) {
            return;
        }

        if (!nameValidator.isValid(newRawName)) {
            rateLimiter.recordFailure(player);
            lang.send(sender, "invalid-pwarp-name", "pwarp", newRawName);
            return;
        }

        if (storage.contains(newRawName)
                && !PwarpNameValidator.canonicalKey(newRawName).equals(PwarpNameValidator.canonicalKey(pwarp.getName()))) {
            rateLimiter.recordFailure(player);
            lang.send(sender, "pwarp-already-exists", "pwarp", newRawName);
            return;
        }

        if (blockPwarpIfAdminWarpExists && isAdminWarpNameTaken(newRawName)) {
            rateLimiter.recordFailure(player);
            lang.send(sender, "admin-warp-name-collision", "pwarp", newRawName);
            return;
        }

        String oldName = pwarp.getName();
        storage.remove(oldName);
        pwarp.setName(newRawName.trim());
        pwarp.touchUpdated();
        storage.put(pwarp);
        logger.logPlayer(player.getName(), player.getUniqueId().toString(), "RENAME", pwarp);
        lang.send(sender, "renamed", "old", oldName, "new", pwarp.getName());
    }

    private boolean isAdminWarpNameTaken(String rawName) {
        AbstractGameSystem abstractSystem = plugin.getSystemManager().findAbstractSystem("warp");
        if (!(abstractSystem instanceof WarpSystem warpSystem) || !warpSystem.isActive()) {
            return false;
        }

        return warpSystem.getStorage() != null && warpSystem.getStorage().contains(rawName);
    }

    private String formatTimestamp(long millis) {
        if (millis <= 0) {
            return neverVisitedText;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateTimeFormat)
                .withZone(ZoneId.systemDefault());
        return formatter.format(Instant.ofEpochMilli(millis));
    }

    private List<String> suggestPwarpNames(String prefix) {
        return storage.getAll().stream()
                .map(Pwarp::getName)
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT)))
                .collect(Collectors.toList());
    }

    private List<String> suggestOwnPwarpNames(Player player, String prefix) {
        return storage.getByOwner(player.getUniqueId()).stream()
                .map(Pwarp::getName)
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT)))
                .collect(Collectors.toList());
    }

    private List<String> suggestOwnerPwarpNames(UUID ownerId, String prefix) {
        return storage.getByOwner(ownerId).stream()
                .map(Pwarp::getName)
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT)))
                .collect(Collectors.toList());
    }

    private List<String> onlinePlayerNames() {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
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

    private String resolveDisplayName(OfflinePlayer player, String fallback) {
        String resolved = player.getName();
        return resolved != null ? resolved : fallback;
    }

    private String resolveActorName(CommandSender sender) {
        if (sender instanceof Player player) {
            return player.getName();
        }
        return "CONSOLE";
    }

    private boolean isBlockedWorld(Location location) {
        return location.getWorld() != null
                && blockedWorlds.contains(location.getWorld().getName().toLowerCase(Locale.ROOT));
    }

    private boolean bypassesTimers(Player player) {
        return PwarpSystem.bypassesPwarpRestrictions(player);
    }

    private boolean isOnCooldown(Player player) {
        return cooldownSeconds > 0 && remainingCooldown(player) > 0;
    }

    private int remainingCooldown(Player player) {
        Long lastUse = teleportCooldowns.get(player.getUniqueId());
        if (lastUse == null) {
            return 0;
        }

        long elapsed = System.currentTimeMillis() - lastUse;
        long cooldownMillis = cooldownSeconds * 1000L;
        if (elapsed >= cooldownMillis) {
            return 0;
        }

        return (int) Math.ceil((cooldownMillis - elapsed) / 1000.0);
    }

    private boolean isOnSetCooldown(Player player) {
        return setCooldownSeconds > 0 && remainingSetCooldown(player) > 0;
    }

    private int remainingSetCooldown(Player player) {
        Long lastUse = setCooldowns.get(player.getUniqueId());
        if (lastUse == null) {
            return 0;
        }

        long elapsed = System.currentTimeMillis() - lastUse;
        long cooldownMillis = setCooldownSeconds * 1000L;
        if (elapsed >= cooldownMillis) {
            return 0;
        }

        return (int) Math.ceil((cooldownMillis - elapsed) / 1000.0);
    }

    private ParsedArgs parseArgs(String[] args) {
        List<String> cleaned = new ArrayList<>();
        boolean confirmed = false;

        for (String arg : args) {
            if (CONFIRM_FLAG.equalsIgnoreCase(arg)) {
                confirmed = true;
            } else {
                cleaned.add(arg);
            }
        }

        return new ParsedArgs(cleaned.toArray(String[]::new), confirmed);
    }

    private boolean isListKeyword(String value) {
        return PwarpSystem.LIST_ALIASES.contains(value.toLowerCase(Locale.ROOT));
    }

    private boolean isSetKeyword(String value) {
        return PwarpSystem.SET_ALIASES.contains(value.toLowerCase(Locale.ROOT));
    }

    private boolean isRemoveKeyword(String value) {
        return PwarpSystem.REMOVE_ALIASES.contains(value.toLowerCase(Locale.ROOT));
    }

    private boolean isEditKeyword(String value) {
        return PwarpSystem.EDIT_ALIASES.contains(value.toLowerCase(Locale.ROOT));
    }

    private boolean isManageKeywordPartial(String value) {
        String lower = value.toLowerCase(Locale.ROOT);
        return PwarpSystem.SET_ALIASES.stream().anyMatch(alias -> alias.startsWith(lower))
                || PwarpSystem.REMOVE_ALIASES.stream().anyMatch(alias -> alias.startsWith(lower))
                || PwarpSystem.EDIT_ALIASES.stream().anyMatch(alias -> alias.startsWith(lower))
                || PwarpSystem.LIST_ALIASES.stream().anyMatch(alias -> alias.startsWith(lower));
    }

    private boolean isEditAction(String value) {
        return PwarpSystem.EDIT_ACTIONS.contains(value.toLowerCase(Locale.ROOT));
    }

    private record ParsedArgs(String[] args, boolean confirmed) {}
}
