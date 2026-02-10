package com.payv.budget.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.YearMonth;
import java.util.Objects;

@Getter
public final class Budget {

    private final BudgetId id;
    private final String ownerUserId;

    private YearMonth targetMonth;
    private long amountLimit;
    private String categoryId; // null = overall budget
    private String memo;
    private boolean isActive;

    @Builder
    private Budget(BudgetId id, String ownerUserId,
                   YearMonth targetMonth, long amountLimit,
                   String categoryId, String memo, boolean isActive) {
        this.id = requireId(id);
        this.ownerUserId = requireOwner(ownerUserId);
        this.targetMonth = requireMonth(targetMonth);
        this.amountLimit = requirePositive(amountLimit);
        this.categoryId = normalizeNullableId(categoryId);
        this.memo = normalizeMemo(memo);
        this.isActive = isActive;
    }

    public static Budget create(String ownerUserId, YearMonth targetMonth, long amountLimit,
                                String categoryId, String memo) {
        return Budget.builder()
                .id(BudgetId.generate())
                .ownerUserId(ownerUserId)
                .targetMonth(targetMonth)
                .amountLimit(amountLimit)
                .categoryId(categoryId)
                .memo(memo)
                .isActive(true)
                .build();
    }

    public static Budget of(BudgetId id, String ownerUserId, YearMonth targetMonth,
                            long amountLimit, String categoryId, String memo, boolean isActive) {
        return Budget.builder()
                .id(id)
                .ownerUserId(ownerUserId)
                .targetMonth(targetMonth)
                .amountLimit(amountLimit)
                .categoryId(categoryId)
                .memo(memo)
                .isActive(isActive)
                .build();
    }

    public void update(YearMonth targetMonth, long amountLimit,
                       String categoryId, String memo) {
        requireActive();
        this.targetMonth = requireMonth(targetMonth);
        this.amountLimit = requirePositive(amountLimit);
        this.categoryId = normalizeNullableId(categoryId);
        this.memo = normalizeMemo(memo);
    }

    public void deactivate() {
        this.isActive = false;
    }

    public boolean isOverallBudget() {
        return categoryId == null;
    }

    public void ensureBelongsTo(String requesterOwnerUserId) {
        if (!Objects.equals(this.ownerUserId, requesterOwnerUserId)) {
            throw new IllegalStateException("budget owner mismatch");
        }
    }

    private void requireActive() {
        if (!isActive) {
            throw new IllegalStateException("inactive budget");
        }
    }

    private static BudgetId requireId(BudgetId id) {
        if (id == null) throw new IllegalArgumentException("budgetId must not be null");
        return id;
    }

    private static String requireOwner(String ownerUserId) {
        if (ownerUserId == null || ownerUserId.trim().isEmpty()) {
            throw new IllegalArgumentException("ownerUserId must not be blank");
        }
        return ownerUserId;
    }

    private static YearMonth requireMonth(YearMonth targetMonth) {
        if (targetMonth == null) {
            throw new IllegalArgumentException("targetMonth must not be null");
        }
        return targetMonth;
    }

    private static long requirePositive(long amount) {
        if (amount <= 0L) {
            throw new IllegalArgumentException("amountLimit must be positive");
        }
        return amount;
    }

    private static String normalizeNullableId(String value) {
        if (value == null) return null;
        String ret = value.trim();
        return ret.isEmpty() ? null : ret;
    }

    private static String normalizeMemo(String memo) {
        if (memo == null) return null;
        String ret = memo.trim();
        return ret.isEmpty() ? null : ret;
    }
}
