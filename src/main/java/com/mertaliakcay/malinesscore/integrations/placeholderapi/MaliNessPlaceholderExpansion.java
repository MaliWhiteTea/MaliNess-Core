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
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        return resolver.resolve(player, params);
    }
}
