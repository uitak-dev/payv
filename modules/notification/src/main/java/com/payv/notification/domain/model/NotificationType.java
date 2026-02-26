package com.payv.notification.domain.model;

public enum NotificationType {
    BUDGET_THRESHOLD_50("예산 50% 초과"),
    BUDGET_THRESHOLD_100("예산 100% 초과"),
    FIXED_EXPENSE_AUTO_CREATED("고정비 자동 생성");

    private final String displayName;

    NotificationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
