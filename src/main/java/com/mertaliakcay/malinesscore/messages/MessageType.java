package com.mertaliakcay.malinesscore.messages;

public enum MessageType {

    WARNING("warning"),
    ERROR("error"),
    NORMAL("normal"),
    SUCCESS("success");

    private final String configKey;

    MessageType(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigKey() {
        return configKey;
    }

    public static MessageType fromString(String value) {
        if (value == null) {
            return NORMAL;
        }

        return switch (value.toLowerCase()) {
            case "warning", "uyari", "uyarı" -> WARNING;
            case "error", "hata" -> ERROR;
            case "success", "basari", "başarı" -> SUCCESS;
            default -> NORMAL;
        };
    }
}
