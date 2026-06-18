package com.mertaliakcay.malinesscore.confirmation;

public record PendingConfirmation(
        String token,
        long expiresAt,
        Runnable onAccept,
        Runnable onDeny
) {

    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }
}
