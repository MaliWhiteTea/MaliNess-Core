package com.mertaliakcay.malinesscore.systems.feed;

import com.mertaliakcay.malinesscore.systems.AbstractGameSystem;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

import java.util.List;

public final class FeedSystem extends AbstractGameSystem {

    public static final String PERM_USE = "maliness-core.feed.use";
    public static final String PERM_OTHERS = "maliness-core.feed.use.others";
    public static final String ALIAS_TURKISH = "doyur";

    private FeedCommand feedCommand;

    @Override
    protected String getSystemId() {
        return "feed";
    }

    @Override
    protected void onEnable() {
        if (!config.get().getBoolean("enabled", true)) {
            return;
        }

        feedCommand = new FeedCommand(this);
        FeedBasicCommand feedBasicCommand = new FeedBasicCommand(feedCommand);

        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            event.registrar().register(
                    "feed",
                    "Açlığı giderir.",
                    List.of(ALIAS_TURKISH),
                    feedBasicCommand
            );
        });

        plugin.getMalinessCommand().setFeed(this, feedCommand);
    }

    @Override
    protected void onDisable() {
        plugin.getMalinessCommand().clearFeed();
        feedCommand = null;
    }

    public boolean isEnabled() {
        return config.get().getBoolean("enabled", true);
    }
}
