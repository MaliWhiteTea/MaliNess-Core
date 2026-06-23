package com.mertaliakcay.malinesscore.systems.economy.model;

public enum TransactionResult {
    SUCCESS,
    INSUFFICIENT_FUNDS,
    INVALID_AMOUNT,
    CURRENCY_NOT_FOUND,
    CURRENCY_DISABLED,
    PLAYER_NOT_FOUND,
    LIMIT_EXCEEDED,
    SAME_ACCOUNT,
    ECONOMY_UNAVAILABLE,
    INTERNAL_ERROR
}
