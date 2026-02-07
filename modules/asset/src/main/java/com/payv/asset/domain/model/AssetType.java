package com.payv.asset.domain.model;

public enum AssetType {
    CARD("카드"),
    CASH("현금"),
    BANK_ACCOUNT("계좌");

    private final String displayName;

    AssetType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
