package com.mertaliakcay.malinesscore.systems.feed;

import com.mertaliakcay.malinesscore.systems.AbstractGameSystem;

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
    protected void onRegister() {
        if (feedCommand == null) {
            feedCommand = new FeedCommand(this);
        }

        registerLifecycleCommandsOnce(registrar -> registrar.register(
                "feed",
                "Açlığı giderir.",
                List.of(ALIAS_TURKISH),
                new FeedBasicCommand(feedCommand)
        ));

        plugin.getMalinessCommand().setFeed(this, feedCommand);
    }

    @Override
    protected void onEnable() {
    }

    @Override
    protected void onDisable() {
    }

    @Override
    protected void onUnregister() {
        plugin.getMalinessCommand().clearFeed();
    }
}
