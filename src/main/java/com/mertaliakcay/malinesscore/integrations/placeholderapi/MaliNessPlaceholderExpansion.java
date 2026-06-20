package com.mertaliakcay.malinesscore.integrations.placeholderapi;

import com.mertaliakcay.malinesscore.MaliNessCore;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MaliNessPlaceholderExpansion extends PlaceholderExpansion {

    private final MaliNessCore plugin;
    private final MaliNessPlaceholderResolver resolver;

    public MaliNessPlaceholderExpansion(MaliNessCore plugin, MaliNessPlaceholderResolver resolver) {
        this.plugin = plugin;
        this.resolver = resolver;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "malinesscore";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getPluginMeta().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @NotNull java.util.List<String> getPlaceholders() {
        return java.util.List.of(
                "version",
                "online",
                "online_visible",
                "online_vanished",
                "online_visible_list",
                "can_see",
                "can_see_<oyuncu>",
                "can_see_bool",
                "can_see_bool_<oyuncu>",
                "vanish",
                "vanish_<oyuncu>",
                "vanish_bool",
                "vanish_bool_<oyuncu>",
                "system_<id>",
                "system_<id>_bool",
                "god",
                "god_<oyuncu>",
                "god_bool",
                "god_bool_<oyuncu>",
                "homes_count",
                "homes_limit",
                "homes_remaining",
                "homes_list",
                "homes_overlimit",
                "home_warmup",
                "home_warmup_active",
                "confirmation_pending",
                "playtime",
                "playtime_seconds"
        );
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        return resolver.resolve(player, params);
    }
}
