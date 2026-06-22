package com.mertaliakcay.malinesscore.gui.content;

import com.mertaliakcay.malinesscore.gui.model.MenuClickType;
import com.mertaliakcay.malinesscore.gui.model.MenuSession;
import org.bukkit.entity.Player;

public interface MenuContentProvider {

    String getId();

    void initialize(MenuSession session);

    int getTotalPages(MenuSession session);

    void populatePage(MenuSession session);

    void onContentClick(Player player, MenuSession session, String entryId, MenuClickType clickType);
}
