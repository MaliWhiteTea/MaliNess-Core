package com.mertaliakcay.malinesscore.systems.control;

import com.mertaliakcay.malinesscore.MaliNessCore;
import com.mertaliakcay.malinesscore.systems.AbstractGameSystem;
import com.mertaliakcay.malinesscore.systems.SystemManager;
import com.mertaliakcay.malinesscore.util.PluginLang;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public final class SystemControlService {

    public static final String ALIAS_SYSTEMS_TR = "sistemler";
    public static final String ALIAS_SYSTEM_TR = "sistem";

    private static final Set<String> ON_ACTIONS = Set.of(
            "on", "enable", "aç", "aktif", "open"
    );
    private static final Set<String> OFF_ACTIONS = Set.of(
            "off", "disable", "kapa", "deaktif", "close"
    );
    private static final Set<String> INFO_ACTIONS = Set.of("info", "bilgi");

    private final MaliNessCore plugin;
    private final SystemManager systemManager;
    private final SystemCatalog catalog;
    private final NonClosableSystemRegistry nonClosableRegistry;
    private final SystemDependencyRegistry dependencyRegistry;
    private final SystemsAuditLogger auditLogger;

    public SystemControlService(
            MaliNessCore plugin,
            SystemManager systemManager,
            NonClosableSystemRegistry nonClosableRegistry,
            SystemDependencyRegistry dependencyRegistry,
            SystemsAuditLogger auditLogger
    ) {
        this.plugin = plugin;
        this.systemManager = systemManager;
        this.nonClosableRegistry = nonClosableRegistry;
        this.dependencyRegistry = dependencyRegistry;
        this.auditLogger = auditLogger;
        this.catalog = new SystemCatalog(systemManager, nonClosableRegistry);
    }

    public SystemCatalog getCatalog() {
        return catalog;
    }

    public NonClosableSystemRegistry getNonClosableRegistry() {
        return nonClosableRegistry;
    }

    public SystemDependencyRegistry getDependencyRegistry() {
        return dependencyRegistry;
    }

    public boolean canList(CommandSender sender) {
        return SystemPermissions.canList(sender, catalog.gameSystemIds());
    }

    public boolean canManage(CommandSender sender, String systemId) {
        return SystemPermissions.canManage(sender, systemId);
    }

    public boolean canViewInfo(CommandSender sender, String systemId) {
        return canList(sender) || canManage(sender, systemId);
    }

    public void refreshCatalog() {
        catalog.rebuild();
    }

    public void handleSystemCommand(CommandSender sender, String[] args) {
        PluginLang lang = plugin.getPluginLang();

        if (args.length < 1) {
            lang.send(sender, "systems-usage-system");
            return;
        }

        String action = normalizeAction(args[0]);
        if (action == null) {
            lang.send(sender, "systems-usage-system");
            return;
        }

        if (args.length < 2) {
            lang.send(sender, "systems-usage-system");
            return;
        }

        String systemId = args[1].toLowerCase(Locale.ROOT);

        if ("info".equals(action)) {
            showInfo(sender, systemId);
            return;
        }

        boolean enable = "on".equals(action);
        requestToggle(sender, systemId, enable);
    }

    public void requestToggle(CommandSender sender, String systemId, boolean enable) {
        PluginLang lang = plugin.getPluginLang();

        if (nonClosableRegistry.isNonClosable(systemId)) {
            lang.send(sender, "systems-non-closable", "system", displayName(systemId));
            return;
        }

        Optional<SystemDescriptor> descriptor = catalog.find(systemId);
        if (descriptor.isEmpty() || descriptor.get().isVirtual()) {
            lang.send(sender, "systems-unknown", "system", systemId);
            return;
        }

        if (!canManage(sender, systemId)) {
            lang.send(sender, "systems-no-manage-permission", "system", displayName(systemId));
            return;
        }

        Optional<AbstractGameSystem> gameSystem = catalog.findGameSystem(systemId);
        if (gameSystem.isEmpty()) {
            lang.send(sender, "systems-unknown", "system", systemId);
            return;
        }

        AbstractGameSystem system = gameSystem.get();
        if (enable && system.isActive()) {
            lang.send(sender, "systems-already-active", "system", displayName(systemId));
            return;
        }
        if (!enable && !system.isActive()) {
            lang.send(sender, "systems-already-inactive", "system", displayName(systemId));
            return;
        }

        if (sender instanceof Player player) {
            Component prompt = buildConfirmationPrompt(systemId, enable);
            plugin.getConfirmationService().request(
                    player,
                    prompt,
                    () -> applyToggle(sender, systemId, enable),
                    null
            );
            return;
        }

        applyToggle(sender, systemId, enable);
    }

    public void applyToggle(CommandSender sender, String systemId, boolean enable) {
        PluginLang lang = plugin.getPluginLang();

        if (!systemManager.setSystemEnabled(systemId, enable)) {
            lang.send(sender, "systems-toggle-failed", "system", displayName(systemId));
            return;
        }

        if (enable) {
            lang.send(sender, "systems-enabled", "system", displayName(systemId));
            auditLogger.log(
                    sender.getName(),
                    senderId(sender),
                    systemId,
                    "ENABLE"
            );
        } else {
            lang.send(sender, "systems-disabled", "system", displayName(systemId));
            auditLogger.log(
                    sender.getName(),
                    senderId(sender),
                    systemId,
                    "DISABLE"
            );
        }

        refreshSystemsList(sender, systemId);
    }

    private void refreshSystemsList(CommandSender sender, String systemId) {
        if (!(sender instanceof Player player) || !canList(sender)) {
            return;
        }

        int page = SystemsListHelp.pageForSystem(this, systemId);
        SystemsListHelp.send(plugin, this, player, page);
    }

    public void showInfo(CommandSender sender, String systemId) {
        PluginLang lang = plugin.getPluginLang();

        Optional<SystemDescriptor> descriptor = catalog.find(systemId);
        if (descriptor.isEmpty()) {
            lang.send(sender, "systems-unknown", "system", systemId);
            return;
        }

        if (sender instanceof Player && !canViewInfo(sender, systemId)) {
            lang.send(sender, "systems-no-list-permission");
            return;
        }

        SystemDescriptor entry = descriptor.get();
        String display = displayName(systemId);
        boolean active = isActive(entry);
        String statusKey = active ? "systems-info-status-active" : "systems-info-status-inactive";
        String closableKey = entry.isClosable() ? "systems-info-closable-yes" : "systems-info-closable-no";

        lang.send(sender, "systems-info-header");
        lang.send(sender, "systems-info-name", "display", display, "id", systemId);
        lang.send(sender, statusKey);
        lang.send(sender, "systems-info-description", "description", lang.getText("systems-desc-" + systemId));
        lang.send(sender, "systems-info-commands", "commands", lang.getText("systems-commands-" + systemId));
        if (!entry.isVirtual()) {
            lang.send(sender, "systems-info-config", "path", entry.configPath());
        }
        lang.send(sender, closableKey);

        if (!entry.isVirtual() && !canManage(sender, systemId)) {
            lang.send(sender, "systems-info-no-manage");
        }
    }

    public String displayName(String systemId) {
        return plugin.getPluginLang().getText("systems-display-" + systemId);
    }

    public boolean isActive(SystemDescriptor descriptor) {
        if (descriptor.isVirtual()) {
            return true;
        }
        return catalog.findGameSystem(descriptor.getId())
                .map(AbstractGameSystem::isActive)
                .orElse(false);
    }

    public Component buildConfirmationPrompt(String systemId, boolean enable) {
        PluginLang lang = plugin.getPluginLang();
        String display = displayName(systemId);
        String key = enable ? "systems-confirm-enable" : "systems-confirm-disable";
        Component prompt = lang.get(key, "system", display);

        List<Component> sideEffects = sideEffectLines(systemId, enable);
        if (sideEffects.isEmpty()) {
            return prompt;
        }

        Component combined = prompt;
        for (Component line : sideEffects) {
            combined = combined.append(Component.newline()).append(line);
        }
        return combined;
    }

    public List<Component> sideEffectLines(String systemId, boolean enable) {
        if (enable) {
            return List.of();
        }

        PluginLang lang = plugin.getPluginLang();
        List<Component> lines = new ArrayList<>();
        String sideEffectKey = "systems-side-effect-" + systemId;
        String text = lang.getText(sideEffectKey);
        if (!text.equals(sideEffectKey) && !text.isBlank()) {
            lines.add(lang.get(sideEffectKey));
        }
        return lines;
    }

    public static String normalizeAction(String raw) {
        if (raw == null) {
            return null;
        }
        String action = raw.toLowerCase(Locale.ROOT);
        if (ON_ACTIONS.contains(action)) {
            return "on";
        }
        if (OFF_ACTIONS.contains(action)) {
            return "off";
        }
        if (INFO_ACTIONS.contains(action)) {
            return "info";
        }
        return null;
    }

    public static List<String> actionSuggestions() {
        return List.of("on", "off", "info", "ac", "aç", "kapa", "bilgi", "enable", "disable");
    }

    public List<String> systemIdSuggestions(CommandSender sender, String prefix) {
        List<String> suggestions = new ArrayList<>();
        for (SystemDescriptor descriptor : catalog.listAll()) {
            if (descriptor.isVirtual()) {
                suggestions.add(descriptor.getId());
                continue;
            }
            suggestions.add(descriptor.getId());
        }
        return filterSuggestions(suggestions, prefix);
    }

    public List<String> manageableSystemSuggestions(CommandSender sender, String prefix) {
        List<String> suggestions = new ArrayList<>();
        for (String systemId : catalog.gameSystemIds()) {
            if (canManage(sender, systemId)) {
                suggestions.add(systemId);
            }
        }
        return filterSuggestions(suggestions, prefix);
    }

    private List<String> filterSuggestions(List<String> values, String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return values;
        }
        String lower = prefix.toLowerCase(Locale.ROOT);
        return values.stream()
                .filter(value -> value.toLowerCase(Locale.ROOT).startsWith(lower))
                .toList();
    }

    private String senderId(CommandSender sender) {
        if (sender instanceof Player player) {
            return player.getUniqueId().toString();
        }
        return "console";
    }
}
