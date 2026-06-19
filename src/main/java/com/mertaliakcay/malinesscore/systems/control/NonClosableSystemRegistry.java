package com.mertaliakcay.malinesscore.systems.control;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Kapatilamayan sistem kimliklerini tutar (or. core).
 */
public final class NonClosableSystemRegistry {

    public static final String CORE_ID = "core";

    private final Set<String> nonClosableIds = new LinkedHashSet<>();

    public NonClosableSystemRegistry() {
        nonClosableIds.add(CORE_ID);
    }

    public void register(String systemId) {
        nonClosableIds.add(systemId.toLowerCase());
    }

    public boolean isNonClosable(String systemId) {
        return nonClosableIds.contains(systemId.toLowerCase());
    }

    public Set<String> getNonClosableIds() {
        return Collections.unmodifiableSet(nonClosableIds);
    }
}
