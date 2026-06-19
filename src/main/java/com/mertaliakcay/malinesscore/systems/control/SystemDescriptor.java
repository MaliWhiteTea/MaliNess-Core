package com.mertaliakcay.malinesscore.systems.control;

public final class SystemDescriptor {

    private final String id;
    private final boolean virtual;
    private final boolean closable;

    public SystemDescriptor(String id, boolean virtual, boolean closable) {
        this.id = id;
        this.virtual = virtual;
        this.closable = closable;
    }

    public String getId() {
        return id;
    }

    public boolean isVirtual() {
        return virtual;
    }

    public boolean isClosable() {
        return closable;
    }

    public String configPath() {
        return "configs/" + id + ".yml";
    }
}
