package com.payv.ledger.domain.model;

public enum TransactionSourceType {

    MANUAL("기록"),
    FIXED_COST_AUTO("고정비");

    private final String displayName;

    TransactionSourceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
