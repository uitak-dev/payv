package com.payv.budget.infrastructure.persistence.mybatis.record;

import com.payv.budget.domain.model.Budget;
import com.payv.budget.domain.model.BudgetId;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.time.YearMonth;

@Data
@NoArgsConstructor
public class BudgetRecord {

    private String budgetId;
    private String ownerUserId;
    private String targetMonth;
    private long amountLimit;
    private String categoryId;
    private String memo;
    private boolean isActive;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @Builder
    private BudgetRecord(String budgetId, String ownerUserId, String targetMonth,
                         long amountLimit, String categoryId, String memo, boolean isActive,
                         OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.budgetId = budgetId;
        this.ownerUserId = ownerUserId;
        this.targetMonth = targetMonth;
        this.amountLimit = amountLimit;
        this.categoryId = categoryId;
        this.memo = memo;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static BudgetRecord toRecord(Budget budget) {
        return BudgetRecord.builder()
                .budgetId(budget.getId().getValue())
                .ownerUserId(budget.getOwnerUserId())
                .targetMonth(budget.getTargetMonth().toString())
                .amountLimit(budget.getAmountLimit())
                .categoryId(budget.getCategoryId())
                .memo(budget.getMemo())
                .isActive(budget.isActive())
                .build();
    }

    public Budget toEntity() {
        return Budget.of(
                BudgetId.of(budgetId),
                ownerUserId,
                YearMonth.parse(targetMonth),
                amountLimit,
                categoryId,
                memo,
                Boolean.TRUE.equals(isActive)
        );
    }
}
