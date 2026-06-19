package com.mertaliakcay.malinesscore.systems.control;

import com.mertaliakcay.malinesscore.systems.AbstractGameSystem;
import com.mertaliakcay.malinesscore.systems.GameSystem;
import com.mertaliakcay.malinesscore.systems.SystemManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class SystemCatalog {

    private final SystemManager systemManager;
    private final NonClosableSystemRegistry nonClosableRegistry;
    private final Map<String, SystemDescriptor> descriptors = new LinkedHashMap<>();

    public SystemCatalog(SystemManager systemManager, NonClosableSystemRegistry nonClosableRegistry) {
        this.systemManager = systemManager;
        this.nonClosableRegistry = nonClosableRegistry;
        rebuild();
    }

    public void rebuild() {
        descriptors.clear();
        descriptors.put(
                NonClosableSystemRegistry.CORE_ID,
                new SystemDescriptor(NonClosableSystemRegistry.CORE_ID, true, false)
        );

        for (GameSystem system : systemManager.getSystems()) {
            if (system instanceof AbstractGameSystem abstractSystem) {
                String id = abstractSystem.getName();
                boolean closable = !nonClosableRegistry.isNonClosable(id);
                descriptors.put(id, new SystemDescriptor(id, false, closable));
            }
        }
    }

    public List<SystemDescriptor> listAll() {
        return Collections.unmodifiableList(new ArrayList<>(descriptors.values()));
    }

    public List<String> gameSystemIds() {
        List<String> ids = new ArrayList<>();
        for (SystemDescriptor descriptor : descriptors.values()) {
            if (!descriptor.isVirtual()) {
                ids.add(descriptor.getId());
            }
        }
        return ids;
    }

    public Optional<SystemDescriptor> find(String systemId) {
        if (systemId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(descriptors.get(systemId.toLowerCase()));
    }

    public boolean isKnown(String systemId) {
        return find(systemId).isPresent();
    }

    public Optional<AbstractGameSystem> findGameSystem(String systemId) {
        return Optional.ofNullable(systemManager.findAbstractSystem(systemId));
    }
}
