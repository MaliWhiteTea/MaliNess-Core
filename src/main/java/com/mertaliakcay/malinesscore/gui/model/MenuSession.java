package com.mertaliakcay.malinesscore.gui.model;

import com.mertaliakcay.malinesscore.gui.holder.MaliNessMenuHolder;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class MenuSession {

    public enum SortMode {
        VISITS,
        NAME,
        RECENT
    }

    public enum FilterMode {
        ALL,
        FAVORITES,
        MINE
    }

    private final UUID playerId;
    private final MenuDefinition definition;
    private final MaliNessMenuHolder holder;
    private Inventory inventory;
    private int currentPage = 1;
    private int maxPage = 1;
    private SortMode sortMode = SortMode.VISITS;
    private FilterMode filterMode = FilterMode.ALL;
    private boolean closingIntentionally;
    private final Map<Integer, String> contentEntryBySlot = new HashMap<>();
    private Map<String, Object> providerState = new HashMap<>();

    public MenuSession(UUID playerId, MenuDefinition definition, MaliNessMenuHolder holder) {
        this.playerId = playerId;
        this.definition = definition;
        this.holder = holder;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public MenuDefinition getDefinition() {
        return definition;
    }

    public MaliNessMenuHolder getHolder() {
        return holder;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
        holder.bind(inventory);
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = Math.max(1, currentPage);
    }

    public int getMaxPage() {
        return maxPage;
    }

    public void setMaxPage(int maxPage) {
        this.maxPage = Math.max(1, maxPage);
        if (currentPage > this.maxPage) {
            currentPage = this.maxPage;
        }
    }

    public SortMode getSortMode() {
        return sortMode;
    }

    public void setSortMode(SortMode sortMode) {
        this.sortMode = sortMode;
    }

    public void cycleSortMode() {
        sortMode = switch (sortMode) {
            case VISITS -> SortMode.NAME;
            case NAME -> SortMode.RECENT;
            case RECENT -> SortMode.VISITS;
        };
        currentPage = 1;
    }

    public FilterMode getFilterMode() {
        return filterMode;
    }

    public void setFilterMode(FilterMode filterMode) {
        this.filterMode = filterMode;
        currentPage = 1;
    }

    public boolean isClosingIntentionally() {
        return closingIntentionally;
    }

    public void setClosingIntentionally(boolean closingIntentionally) {
        this.closingIntentionally = closingIntentionally;
    }

    public Map<Integer, String> getContentEntryBySlot() {
        return contentEntryBySlot;
    }

    public void clearContentEntries() {
        contentEntryBySlot.clear();
    }

    public Map<String, Object> getProviderState() {
        return providerState;
    }

    public void setProviderState(Map<String, Object> providerState) {
        this.providerState = new HashMap<>(providerState);
    }

    public int getPrevStackCount() {
        return Math.min(Math.max(0, currentPage - 1), 64);
    }

    public int getNextStackCount() {
        if (currentPage >= maxPage) {
            return 1;
        }
        return Math.min(currentPage + 1, 64);
    }

    public int getPrevTargetPage() {
        return Math.max(1, currentPage - 1);
    }

    public int getNextTargetPage() {
        return Math.min(maxPage, currentPage + 1);
    }
}
