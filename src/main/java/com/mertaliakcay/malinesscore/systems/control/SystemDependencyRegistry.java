package com.mertaliakcay.malinesscore.systems.control;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Sistem bagimliliklari icin bos altyapi. Ileride kurallar buraya eklenecek.
 */
public final class SystemDependencyRegistry {

    private final Map<String, Set<String>> dependencies = new HashMap<>();

    public void registerDependency(String systemId, String dependsOnSystemId) {
        dependencies
                .computeIfAbsent(systemId.toLowerCase(), ignored -> new HashSet<>())
                .add(dependsOnSystemId.toLowerCase());
    }

    public Set<String> getDependencies(String systemId) {
        Set<String> deps = dependencies.get(systemId.toLowerCase());
        if (deps == null) {
            return Set.of();
        }
        return Collections.unmodifiableSet(deps);
    }

    public boolean wouldBreakDependency(String systemId, boolean enabling) {
        if (enabling) {
            return false;
        }
        return !getDependencies(systemId).isEmpty();
    }
}
