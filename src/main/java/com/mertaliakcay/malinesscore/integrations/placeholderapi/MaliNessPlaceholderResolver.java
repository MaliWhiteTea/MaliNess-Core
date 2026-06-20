package com.mertaliakcay.malinesscore.integrations.placeholderapi;

import com.mertaliakcay.malinesscore.MaliNessCore;
import com.mertaliakcay.malinesscore.confirmation.ConfirmationService;
import com.mertaliakcay.malinesscore.systems.AbstractGameSystem;
import com.mertaliakcay.malinesscore.systems.control.NonClosableSystemRegistry;
import com.mertaliakcay.malinesscore.systems.god.GodSystem;
import com.mertaliakcay.malinesscore.systems.home.HomeLimitService;
import com.mertaliakcay.malinesscore.systems.home.HomeService;
import com.mertaliakcay.malinesscore.systems.home.HomeStorage;
import com.mertaliakcay.malinesscore.systems.home.HomeSystem;
import com.mertaliakcay.malinesscore.systems.home.HomeTeleportManager;
import com.mertaliakcay.malinesscore.systems.playtime.PlaytimeService;
import com.mertaliakcay.malinesscore.systems.playtime.PlaytimeSystem;
import com.mertaliakcay.malinesscore.systems.vanish.VanishService;
import com.mertaliakcay.malinesscore.systems.home.model.PlayerHomes;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.UUID;

public final class MaliNessPlaceholderResolver {

    private static final String[] PLAYER_SUFFIX_KEYS = {
            "confirmation_pending",
            "home_warmup_active",
            "homes_overlimit",
            "homes_remaining",
            "homes_count",
            "homes_limit",
            "homes_list",
            "home_warmup",
            "playtime_seconds",
            "playtime",
            "vanish_bool",
            "vanish",
            "can_see_bool",
            "can_see",
            "god_bool",
            "god"
    };

    private final MaliNessCore plugin;
    private final PlaceholderApiSettings settings;

    public MaliNessPlaceholderResolver(MaliNessCore plugin, PlaceholderApiSettings settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    public String resolve(Player viewer, String params) {
        if (params == null || params.isBlank()) {
            return null;
        }

        String normalized = params.toLowerCase(Locale.ROOT);

        if (normalized.equals("version")) {
            return plugin.getPluginMeta().getVersion();
        }

        if (normalized.equals("online_visible") || normalized.equals("online")) {
            return String.valueOf(resolveOnlineVisible(viewer));
        }

        if (normalized.equals("online_vanished")) {
            return String.valueOf(resolveOnlineVanished());
        }

        if (normalized.equals("online_visible_list")) {
            return resolveOnlineVisibleList(viewer);
        }

        if (normalized.startsWith("system_")) {
            return resolveSystem(normalized);
        }

        TargetKey targetKey = parseTargetKey(normalized);
        if (targetKey == null) {
            return null;
        }

        return switch (targetKey.baseKey()) {
            case "god" -> onOffLabel(resolveGodEnabled(viewer, targetKey.targetName()));
            case "god_bool" -> boolLabel(resolveGodEnabled(viewer, targetKey.targetName()));
            case "homes_count" -> String.valueOf(resolveHomesCount(viewer, targetKey.targetName()));
            case "homes_limit" -> String.valueOf(resolveHomesLimit(viewer, targetKey.targetName()));
            case "homes_remaining" -> String.valueOf(resolveHomesRemaining(viewer, targetKey.targetName()));
            case "homes_list" -> resolveHomesList(viewer, targetKey.targetName());
            case "homes_overlimit" -> yesNoLabel(resolveHomesOverLimit(viewer, targetKey.targetName()));
            case "home_warmup" -> String.valueOf(resolveHomeWarmup(viewer, targetKey.targetName()));
            case "home_warmup_active" -> yesNoLabel(resolveHomeWarmupActive(viewer, targetKey.targetName()));
            case "confirmation_pending" -> yesNoLabel(resolveConfirmationPending(viewer, targetKey.targetName()));
            case "playtime" -> resolvePlaytimeFormatted(viewer, targetKey.targetName());
            case "playtime_seconds" -> String.valueOf(resolvePlaytimeSeconds(viewer, targetKey.targetName()));
            case "vanish" -> onOffLabel(resolveVanish(viewer, targetKey.targetName()));
            case "vanish_bool" -> boolLabel(resolveVanish(viewer, targetKey.targetName()));
            case "can_see" -> yesNoLabel(resolveCanSee(viewer, targetKey.targetName()));
            case "can_see_bool" -> boolLabel(resolveCanSee(viewer, targetKey.targetName()));
            default -> null;
        };
    }

    private String resolveSystem(String params) {
        if (params.endsWith("_bool")) {
            String systemId = params.substring("system_".length(), params.length() - "_bool".length());
            return boolLabel(isSystemActive(systemId));
        }

        String systemId = params.substring("system_".length());
        return onOffLabel(isSystemActive(systemId));
    }

    private TargetKey parseTargetKey(String params) {
        for (String baseKey : PLAYER_SUFFIX_KEYS) {
            if (params.equals(baseKey)) {
                return new TargetKey(baseKey, null);
            }

            String prefix = baseKey + "_";
            if (params.startsWith(prefix)) {
                return new TargetKey(baseKey, params.substring(prefix.length()));
            }
        }

        return null;
    }

    private boolean resolveGodEnabled(Player viewer, String targetName) {
        Player target = resolveOnlinePlayer(viewer, targetName);
        if (target == null) {
            return false;
        }

        GodSystem godSystem = getGodSystem();
        if (godSystem == null || !godSystem.isActive()) {
            return false;
        }

        return godSystem.isGod(target);
    }

    private int resolveHomesCount(Player viewer, String targetName) {
        PlayerHomes homes = loadHomes(viewer, targetName);
        return homes == null ? 0 : homes.size();
    }

    private int resolveHomesLimit(Player viewer, String targetName) {
        HomeLimitService limitService = getHomeLimitService();
        if (limitService == null) {
            return 0;
        }

        OfflinePlayer target = resolveOfflinePlayer(viewer, targetName);
        if (target == null) {
            return 0;
        }

        return limitService.getMaxHomes(target);
    }

    private int resolveHomesRemaining(Player viewer, String targetName) {
        PlayerHomes homes = loadHomes(viewer, targetName);
        int count = homes == null ? 0 : homes.size();
        int limit = resolveHomesLimit(viewer, targetName);
        return Math.max(0, limit - count);
    }

    private String resolveHomesList(Player viewer, String targetName) {
        PlayerHomes homes = loadHomes(viewer, targetName);
        if (homes == null || homes.size() == 0) {
            return "";
        }

        return String.join(", ", homes.getHomeNames());
    }

    private boolean resolveHomesOverLimit(Player viewer, String targetName) {
        HomeLimitService limitService = getHomeLimitService();
        PlayerHomes homes = loadHomes(viewer, targetName);
        if (limitService == null || homes == null) {
            return false;
        }

        OfflinePlayer target = resolveOfflinePlayer(viewer, targetName);
        if (target == null) {
            return false;
        }

        return limitService.isOverLimit(target, homes);
    }

    private int resolveHomeWarmup(Player viewer, String targetName) {
        UUID playerId = resolvePlayerId(viewer, targetName);
        if (playerId == null) {
            return 0;
        }

        return plugin.getHomeTeleportManager().getWarmupRemainingSeconds(playerId);
    }

    private boolean resolveHomeWarmupActive(Player viewer, String targetName) {
        UUID playerId = resolvePlayerId(viewer, targetName);
        if (playerId == null) {
            return false;
        }

        return plugin.getHomeTeleportManager().hasWarmup(playerId);
    }

    private boolean resolveConfirmationPending(Player viewer, String targetName) {
        UUID playerId = resolvePlayerId(viewer, targetName);
        if (playerId == null) {
            return false;
        }

        ConfirmationService confirmationService = plugin.getConfirmationService();
        return confirmationService != null && confirmationService.hasPending(playerId);
    }

    private String resolvePlaytimeFormatted(Player viewer, String targetName) {
        PlaytimeService service = getPlaytimeService();
        if (service == null) {
            return "0sn";
        }

        OfflinePlayer target = resolveOfflinePlayer(viewer, targetName);
        if (target == null) {
            return "0sn";
        }

        return service.getFormatted(target);
    }

    private long resolvePlaytimeSeconds(Player viewer, String targetName) {
        PlaytimeService service = getPlaytimeService();
        if (service == null) {
            return 0L;
        }

        OfflinePlayer target = resolveOfflinePlayer(viewer, targetName);
        if (target == null) {
            return 0L;
        }

        return service.getTotalSeconds(target);
    }

    private boolean resolveVanish(Player viewer, String targetName) {
        VanishService vanishService = plugin.getVanishService();
        if (vanishService == null) {
            return false;
        }

        if (targetName == null || targetName.isBlank()) {
            return viewer != null && vanishService.isVanished(viewer.getUniqueId());
        }

        Player online = Bukkit.getPlayerExact(targetName);
        if (online != null) {
            return vanishService.isVanished(online);
        }

        OfflinePlayer offline = Bukkit.getOfflinePlayer(targetName);
        if (offline.hasPlayedBefore() || offline.isOnline()) {
            return vanishService.isVanished(offline.getUniqueId());
        }

        return false;
    }

    private PlaytimeService getPlaytimeService() {
        AbstractGameSystem system = plugin.getSystemManager().findAbstractSystem("playtime");
        if (system instanceof PlaytimeSystem playtimeSystem) {
            return playtimeSystem.getPlaytimeService();
        }
        return null;
    }

    private int resolveOnlineVisible(Player viewer) {
        VanishService vanishService = plugin.getVanishService();
        if (vanishService == null) {
            return Bukkit.getOnlinePlayers().size();
        }

        return vanishService.countVisibleOnline(viewer);
    }

    private int resolveOnlineVanished() {
        VanishService vanishService = plugin.getVanishService();
        if (vanishService == null) {
            return 0;
        }

        return vanishService.countOnlineVanished();
    }

    private String resolveOnlineVisibleList(Player viewer) {
        VanishService vanishService = plugin.getVanishService();
        if (vanishService == null) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .collect(java.util.stream.Collectors.joining(", "));
        }

        return vanishService.formatVisibleOnlineNames(viewer);
    }

    private boolean resolveCanSee(Player viewer, String targetName) {
        VanishService vanishService = plugin.getVanishService();
        if (vanishService == null) {
            return true;
        }

        Player target = resolveOnlinePlayer(viewer, targetName);
        if (target == null) {
            return true;
        }

        if (viewer == null) {
            return !vanishService.isVanished(target);
        }

        return vanishService.canSee(viewer, target);
    }

    private PlayerHomes loadHomes(Player viewer, String targetName) {
        HomeStorage storage = getHomeStorage();
        if (storage == null) {
            return null;
        }

        OfflinePlayer target = resolveOfflinePlayer(viewer, targetName);
        if (target == null) {
            return null;
        }

        return storage.load(target.getUniqueId());
    }

    private boolean isSystemActive(String systemId) {
        if (NonClosableSystemRegistry.CORE_ID.equalsIgnoreCase(systemId)) {
            return true;
        }

        AbstractGameSystem system = plugin.getSystemManager().findAbstractSystem(systemId);
        return system != null && system.isActive();
    }

    private GodSystem getGodSystem() {
        AbstractGameSystem system = plugin.getSystemManager().findAbstractSystem("god");
        return system instanceof GodSystem godSystem ? godSystem : null;
    }

    private HomeService getHomeService() {
        AbstractGameSystem system = plugin.getSystemManager().findAbstractSystem("home");
        if (system instanceof HomeSystem homeSystem) {
            return homeSystem.getHomeService();
        }
        return null;
    }

    private HomeStorage getHomeStorage() {
        HomeService homeService = getHomeService();
        return homeService == null ? null : homeService.getStorage();
    }

    private HomeLimitService getHomeLimitService() {
        HomeService homeService = getHomeService();
        return homeService == null ? null : homeService.getLimitService();
    }

    private Player resolveOnlinePlayer(Player viewer, String targetName) {
        if (targetName == null || targetName.isBlank()) {
            return viewer != null && viewer.isOnline() ? viewer : null;
        }

        Player online = Bukkit.getPlayerExact(targetName);
        return online != null && online.isOnline() ? online : null;
    }

    private OfflinePlayer resolveOfflinePlayer(Player viewer, String targetName) {
        if (targetName == null || targetName.isBlank()) {
            return viewer;
        }

        Player online = Bukkit.getPlayerExact(targetName);
        if (online != null) {
            return online;
        }

        OfflinePlayer offline = Bukkit.getOfflinePlayer(targetName);
        return offline.hasPlayedBefore() || offline.isOnline() ? offline : null;
    }

    private UUID resolvePlayerId(Player viewer, String targetName) {
        if (targetName == null || targetName.isBlank()) {
            return viewer == null ? null : viewer.getUniqueId();
        }

        Player online = Bukkit.getPlayerExact(targetName);
        if (online != null) {
            return online.getUniqueId();
        }

        OfflinePlayer offline = Bukkit.getOfflinePlayer(targetName);
        if (offline.hasPlayedBefore() || offline.isOnline()) {
            return offline.getUniqueId();
        }

        return null;
    }

    private String onOffLabel(boolean enabled) {
        return enabled ? settings.onLabel() : settings.offLabel();
    }

    private String yesNoLabel(boolean value) {
        return value ? settings.yesLabel() : settings.noLabel();
    }

    private String boolLabel(boolean value) {
        return Boolean.toString(value);
    }

    private record TargetKey(String baseKey, String targetName) {
    }
}
