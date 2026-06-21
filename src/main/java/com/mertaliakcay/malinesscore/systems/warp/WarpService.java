package com.mertaliakcay.malinesscore.systems.warp;

import com.mertaliakcay.malinesscore.MaliNessCore;
import com.mertaliakcay.malinesscore.confirmation.ConfirmationService;
import com.mertaliakcay.malinesscore.systems.warp.model.Warp;
import com.mertaliakcay.malinesscore.teleport.SafeTeleport;
import com.mertaliakcay.malinesscore.teleport.TeleportMessages;
import com.mertaliakcay.malinesscore.teleport.TeleportService;
import com.mertaliakcay.malinesscore.teleport.WarmupType;
import com.mertaliakcay.malinesscore.util.CommandSuggestions;
import com.mertaliakcay.malinesscore.util.MaliNessColorUtil;
import com.mertaliakcay.malinesscore.util.SystemLang;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class WarpService {

    private static final String CONFIRM_FLAG = "--confirm";

    private final MaliNessCore plugin;
    private final WarpSystem system;
    private final WarpStorage storage;
    private final WarpNameValidator nameValidator;
    private final WarpLogger logger;
    private final TeleportService teleportService;
    private final ConfirmationService confirmationService;

    private final Map<UUID, Long> teleportCooldowns = new ConcurrentHashMap<>();

    private List<String> blockedWorlds = List.of();
    private int warmupSeconds = 5;
    private int cooldownSeconds = 5;
    private int fireResistanceSeconds = 3;
    private int maxWarps = -1;
    private boolean safeTeleportEnabled = true;
    private boolean askOnUnsafe = true;

    public WarpService(
            MaliNessCore plugin,
            WarpSystem system,
            WarpStorage storage,
            WarpNameValidator nameValidator,
            WarpLogger logger,
            TeleportService teleportService,
            ConfirmationService confirmationService
    ) {
        this.plugin = plugin;
        this.system = system;
        this.storage = storage;
        this.nameValidator = nameValidator;
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
        fireResistanceSeconds = configuration.getInt("teleport.fire-resistance-seconds", 3);
        maxWarps = configuration.getInt("max-warps", -1);
        safeTeleportEnabled = configuration.getBoolean("safe-teleport.enabled", true);
        askOnUnsafe = configuration.getBoolean("safe-teleport.ask-on-unsafe", true);
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

        if (isAdminKeyword(args[0])) {
            handleAdmin(sender, args);
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
            if (sender.hasPermission(WarpSystem.PERM_MANAGE)) {
                suggestions.addAll(WarpSystem.SET_ALIASES);
                suggestions.addAll(WarpSystem.REMOVE_ALIASES);
                suggestions.addAll(WarpSystem.EDIT_ALIASES);
            }
            suggestions.addAll(suggestWarpNames(sender, ""));
            return CommandSuggestions.filter(suggestions, "");
        }

        if (args.length == 1) {
            if (sender.hasPermission(WarpSystem.PERM_MANAGE) && isAdminKeywordPartial(args[0])) {
                List<String> admin = new ArrayList<>();
                admin.addAll(WarpSystem.SET_ALIASES);
                admin.addAll(WarpSystem.REMOVE_ALIASES);
                admin.addAll(WarpSystem.EDIT_ALIASES);
                return CommandSuggestions.filter(admin, args[0]);
            }

            if (args[0].matches("\\d*")) {
                List<String> pageSuggestions = List.of("1", "2", "3");
                List<String> merged = new ArrayList<>(pageSuggestions);
                merged.addAll(suggestWarpNames(sender, args[0]));
                return CommandSuggestions.filter(merged, args[0]);
            }

            return CommandSuggestions.filter(suggestWarpNames(sender, args[0]), args[0]);
        }

        if (args.length == 2 && sender.hasPermission(WarpSystem.PERM_MANAGE) && isAdminKeyword(args[0])) {
            if (isSetKeyword(args[0])) {
                return CommandSuggestions.filter(List.of(), args[1]);
            }
            return CommandSuggestions.filter(suggestAllWarpNames(args[1]), args[1]);
        }

        if (args.length == 3 && sender.hasPermission(WarpSystem.PERM_MANAGE) && isEditKeyword(args[0])) {
            Warp warp = storage.find(args[1]);
            if (warp == null) {
                return List.of();
            }

            List<String> editOptions = new ArrayList<>(WarpSystem.EDIT_ACTIONS);
            editOptions.addAll(WarpSystem.ENABLE_ACTIONS);
            editOptions.addAll(WarpSystem.DISABLE_ACTIONS);
            if (!isEditAction(args[2])) {
                editOptions.addAll(suggestAllWarpNames(args[2]));
            }
            return CommandSuggestions.filter(editOptions, args[2]);
        }

        return List.of();
    }

    public Collection<Warp> getVisibleWarps(CommandSender viewer) {
        List<Warp> visible = new ArrayList<>();
        for (Warp warp : storage.getAll()) {
            if (warp.isEnabled() || canSeeClosed(viewer)) {
                visible.add(warp);
            }
        }
        return visible;
    }

    public List<Warp> getOpenWarps() {
        return storage.getAll().stream().filter(Warp::isEnabled).toList();
    }

    public boolean canSeeClosed(CommandSender sender) {
        return sender.hasPermission(WarpSystem.PERM_SEE_CLOSED);
    }

    public void sendList(CommandSender sender, int page) {
        if (!sender.hasPermission(WarpSystem.PERM_USE)) {
            system.getLang().send(sender, "no-permission");
            return;
        }

        WarpListHelp.send(plugin, this, sender, page);
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

        if (!sender.hasPermission(WarpSystem.PERM_USE)) {
            lang.send(sender, "no-permission");
            return;
        }

        Warp warp = storage.find(rawName);
        if (warp == null) {
            lang.send(sender, "warp-not-found", "warp", rawName);
            return;
        }

        if (!warp.isEnabled()) {
            if (!canSeeClosed(sender)) {
                lang.send(sender, "warp-not-found", "warp", rawName);
                return;
            }

            confirmationService.request(
                    player,
                    lang.get("confirm-closed-teleport", "warp", warp.getName()),
                    () -> teleportToWarp(player, warp),
                    null
            );
            return;
        }

        teleportToWarp(player, warp);
    }

    private void teleportToWarp(Player player, Warp warp) {
        SystemLang lang = system.getLang();

        Location location = warp.toLocation();
        if (location == null || location.getWorld() == null) {
            lang.send(player, "world-not-loaded");
            storage.validateWorlds();
            return;
        }

        Runnable onSuccess = () -> {
            teleportCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
            logger.logTeleport(player.getName(), player.getUniqueId().toString(), warp);
            lang.send(player, "teleport-success", "warp", warp.getName());
        };

        if (safeTeleportEnabled && !SafeTeleport.isSafe(location)) {
            if (askOnUnsafe) {
                confirmationService.request(
                        player,
                        lang.get("confirm-unsafe-teleport", "warp", warp.getName()),
                        () -> startTeleport(player, warp, onSuccess),
                        null
                );
                return;
            }

            lang.send(player, "unsafe-warp-blocked", "warp", warp.getName());
            return;
        }

        startTeleport(player, warp, onSuccess);
    }

    private void startTeleport(Player player, Warp warp, Runnable onSuccess) {
        SystemLang lang = system.getLang();
        TeleportMessages messages = TeleportMessages.fromSystemLang(lang);

        if (!bypassesTimers(player) && isOnCooldown(player)) {
            lang.send(player, "warp-cooldown", "seconds", remainingCooldown(player));
            return;
        }

        if (player.isInsideVehicle() && !WarpSystem.bypassesWarpRestrictions(player)) {
            lang.send(player, "teleport-vehicle-blocked");
            return;
        }

        if (blockIfWarmupPending(player, lang)) {
            return;
        }

        if (bypassesTimers(player)) {
            teleportService.teleportInstant(player, warp, messages, fireResistanceSeconds, onSuccess);
            return;
        }

        lang.send(player, "teleport-started", "warp", warp.getName(), "seconds", warmupSeconds);
        teleportService.startWarmup(
                player,
                warp,
                messages,
                warmupSeconds,
                fireResistanceSeconds,
                WarpSystem::bypassesWarpRestrictions,
                WarmupType.WARP,
                onSuccess
        );
    }

    private boolean blockIfWarmupPending(Player player, SystemLang lang) {
        if (!teleportService.hasWarmup(player.getUniqueId())) {
            return false;
        }

        WarmupType type = teleportService.getWarmupType(player.getUniqueId());
        if (type == WarmupType.WARP) {
            lang.send(player, "teleport-already-pending");
        } else {
            lang.send(player, "teleport-blocked-by-home");
        }
        return true;
    }

    private void handleAdmin(CommandSender sender, String[] args) {
        SystemLang lang = system.getLang();
        if (!sender.hasPermission(WarpSystem.PERM_MANAGE)) {
            lang.send(sender, "no-permission-manage");
            return;
        }

        if (isSetKeyword(args[0])) {
            if (args.length != 2) {
                lang.send(sender, "usage-set");
                return;
            }
            handleCreate(sender, args[1]);
            return;
        }

        if (isRemoveKeyword(args[0])) {
            if (args.length < 2 || args.length > 3) {
                lang.send(sender, "usage-remove");
                return;
            }
            boolean confirmed = args.length == 3 && CONFIRM_FLAG.equalsIgnoreCase(args[2]);
            handleDelete(sender, args[1], confirmed);
            return;
        }

        if (isEditKeyword(args[0])) {
            handleEdit(sender, args);
            return;
        }

        lang.send(sender, "usage");
    }

    private void handleCreate(CommandSender sender, String rawName) {
        SystemLang lang = system.getLang();
        if (!(sender instanceof Player player)) {
            lang.send(sender, "console-set-not-supported");
            return;
        }

        if (!nameValidator.isValid(rawName)) {
            lang.send(sender, "invalid-warp-name", "warp", rawName);
            return;
        }

        if (storage.contains(rawName)) {
            lang.send(sender, "warp-already-exists", "warp", rawName);
            return;
        }

        if (isAtMaxWarps()) {
            lang.send(sender, "warp-limit-reached", "limit", maxWarps);
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

        Warp warp = Warp.fromLocation(rawName.trim(), location);
        storage.put(warp);
        logger.logAdmin(player.getName(), "CREATE", warp);
        lang.send(sender, "created", "warp", warp.getName());
    }

    private void handleDelete(CommandSender sender, String rawName, boolean confirmed) {
        SystemLang lang = system.getLang();
        Warp warp = storage.find(rawName);
        if (warp == null) {
            lang.send(sender, "warp-not-found", "warp", rawName);
            return;
        }

        if (!confirmed) {
            if (sender instanceof Player player) {
                confirmationService.request(
                        player,
                        lang.get("confirm-delete", "warp", warp.getName()),
                        () -> handleDelete(sender, warp.getName(), true),
                        null
                );
                return;
            }

            lang.send(sender, "console-confirm-required");
            return;
        }

        storage.remove(warp.getName());
        logger.logAdmin(resolveAdminName(sender), "DELETE", warp);
        lang.send(sender, "deleted", "warp", warp.getName());
    }

    private void handleEdit(CommandSender sender, String[] args) {
        SystemLang lang = system.getLang();
        if (args.length < 3) {
            lang.send(sender, "usage-edit");
            return;
        }

        Warp warp = storage.find(args[1]);
        if (warp == null) {
            lang.send(sender, "warp-not-found", "warp", args[1]);
            return;
        }

        String action = args[2].toLowerCase(Locale.ROOT);

        if (WarpSystem.LOCATION_ACTIONS.contains(action)) {
            if (!(sender instanceof Player player)) {
                lang.send(sender, "console-location-not-supported");
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

            warp.updateLocation(location);
            storage.put(warp);
            logger.logAdmin(resolveAdminName(sender), "RELOCATE", warp);
            lang.send(sender, "relocated", "warp", warp.getName());
            return;
        }

        if (WarpSystem.DESCRIPTION_ACTIONS.contains(action)) {
            String description = args.length <= 3
                    ? ""
                    : String.join(" ", java.util.Arrays.copyOfRange(args, 3, args.length));
            if (sender instanceof Player player) {
                description = MaliNessColorUtil.apply(description, player, plugin);
            }
            warp.setDescription(description);
            warp.touchUpdated();
            storage.put(warp);
            logger.logAdminDescription(resolveAdminName(sender), warp.getName(), description);
            if (description.isBlank()) {
                lang.send(sender, "description-cleared", "warp", warp.getName());
            } else {
                lang.send(sender, "description-updated", "warp", warp.getName());
            }
            return;
        }

        if (WarpSystem.ENABLE_ACTIONS.contains(action)) {
            if (warp.isEnabled()) {
                lang.send(sender, "already-open", "warp", warp.getName());
                return;
            }
            warp.setEnabled(true);
            warp.touchUpdated();
            storage.put(warp);
            logger.logAdmin(resolveAdminName(sender), "ENABLE", warp);
            lang.send(sender, "enabled", "warp", warp.getName());
            return;
        }

        if (WarpSystem.DISABLE_ACTIONS.contains(action)) {
            if (!warp.isEnabled()) {
                lang.send(sender, "already-closed", "warp", warp.getName());
                return;
            }
            warp.setEnabled(false);
            warp.touchUpdated();
            storage.put(warp);
            logger.logAdmin(resolveAdminName(sender), "DISABLE", warp);
            lang.send(sender, "disabled", "warp", warp.getName());
            return;
        }

        if (args.length == 3) {
            handleRename(sender, warp, args[2]);
            return;
        }

        lang.send(sender, "usage-edit");
    }

    private void handleRename(CommandSender sender, Warp warp, String newRawName) {
        SystemLang lang = system.getLang();
        if (!nameValidator.isValid(newRawName)) {
            lang.send(sender, "invalid-warp-name", "warp", newRawName);
            return;
        }

        if (storage.contains(newRawName) && !WarpNameValidator.canonicalKey(newRawName).equals(WarpNameValidator.canonicalKey(warp.getName()))) {
            lang.send(sender, "warp-already-exists", "warp", newRawName);
            return;
        }

        String oldName = warp.getName();
        storage.remove(oldName);
        warp.setName(newRawName.trim());
        warp.touchUpdated();
        storage.put(warp);
        logger.logAdminRename(resolveAdminName(sender), oldName, warp.getName());
        lang.send(sender, "renamed", "old", oldName, "new", warp.getName());
    }

    private List<String> suggestWarpNames(CommandSender sender, String prefix) {
        return getVisibleWarps(sender).stream()
                .map(Warp::getName)
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT)))
                .collect(Collectors.toList());
    }

    private List<String> suggestAllWarpNames(String prefix) {
        return storage.getAll().stream()
                .map(Warp::getName)
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT)))
                .collect(Collectors.toList());
    }

    private boolean isAtMaxWarps() {
        return maxWarps > 0 && storage.size() >= maxWarps;
    }

    private boolean isBlockedWorld(Location location) {
        return location.getWorld() != null
                && blockedWorlds.contains(location.getWorld().getName().toLowerCase(Locale.ROOT));
    }

    private boolean bypassesTimers(Player player) {
        return WarpSystem.bypassesWarpRestrictions(player);
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

    private String resolveAdminName(CommandSender sender) {
        if (sender instanceof Player player) {
            return player.getName();
        }
        return "CONSOLE";
    }

    private boolean isAdminKeyword(String value) {
        return isSetKeyword(value) || isRemoveKeyword(value) || isEditKeyword(value);
    }

    private boolean isAdminKeywordPartial(String value) {
        String lower = value.toLowerCase(Locale.ROOT);
        return WarpSystem.SET_ALIASES.stream().anyMatch(alias -> alias.startsWith(lower))
                || WarpSystem.REMOVE_ALIASES.stream().anyMatch(alias -> alias.startsWith(lower))
                || WarpSystem.EDIT_ALIASES.stream().anyMatch(alias -> alias.startsWith(lower));
    }

    private boolean isSetKeyword(String value) {
        return WarpSystem.SET_ALIASES.contains(value.toLowerCase(Locale.ROOT));
    }

    private boolean isRemoveKeyword(String value) {
        return WarpSystem.REMOVE_ALIASES.contains(value.toLowerCase(Locale.ROOT));
    }

    private boolean isEditKeyword(String value) {
        return WarpSystem.EDIT_ALIASES.contains(value.toLowerCase(Locale.ROOT));
    }

    private boolean isEditAction(String value) {
        String lower = value.toLowerCase(Locale.ROOT);
        return WarpSystem.EDIT_ACTIONS.contains(lower)
                || WarpSystem.ENABLE_ACTIONS.contains(lower)
                || WarpSystem.DISABLE_ACTIONS.contains(lower);
    }
}
