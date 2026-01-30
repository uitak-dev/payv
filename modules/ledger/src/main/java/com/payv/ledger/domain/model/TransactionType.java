package com.payv.ledger.domain.model;

public enum TransactionType {
    INCOME("수입"),
    EXPENSE("지출");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
