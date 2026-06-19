package com.mertaliakcay.malinesscore.systems.home.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class PlayerHomes {

    private final Map<String, HomeLocation> homes = new LinkedHashMap<>();

    public Map<String, HomeLocation> getHomes() {
        return Collections.unmodifiableMap(homes);
    }

    public Set<String> getHomeNames() {
        return homes.keySet();
    }

    public HomeLocation get(String name) {
        return homes.get(name);
    }

    public void put(String name, HomeLocation location) {
        homes.put(name, location);
    }

    public HomeLocation remove(String name) {
        return homes.remove(name);
    }

    public int size() {
        return homes.size();
    }

    public boolean contains(String name) {
        return homes.containsKey(name);
    }

    public static PlayerHomes copyOf(PlayerHomes source) {
        PlayerHomes copy = new PlayerHomes();
        for (var entry : source.getHomes().entrySet()) {
            copy.put(entry.getKey(), entry.getValue());
        }
        return copy;
    }
}
