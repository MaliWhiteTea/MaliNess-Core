package com.mertaliakcay.malinesscore.gui.model;

public final class EconomyMenuBehavior {

    private final EconomyOutcome onInsufficient;
    private final EconomyOutcome onSuccess;
    private final EconomyOutcome onError;

    public EconomyMenuBehavior(EconomyOutcome onInsufficient, EconomyOutcome onSuccess, EconomyOutcome onError) {
        this.onInsufficient = onInsufficient;
        this.onSuccess = onSuccess;
        this.onError = onError;
    }

    public EconomyOutcome getOnInsufficient() {
        return onInsufficient;
    }

    public EconomyOutcome getOnSuccess() {
        return onSuccess;
    }

    public EconomyOutcome getOnError() {
        return onError;
    }
}
