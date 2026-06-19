package com.mertaliakcay.malinesscore.confirmation;

public record PendingConfirmation(
        long expiresAt,
        Runnable onAccept,
        Runnable onDeny
) {

    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }
}
