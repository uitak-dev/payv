package com.payv.contracts.budget.dto;

import java.time.YearMonth;

public class BudgetUsagePublicDto {

    private final String budgetId;
    private final YearMonth targetMonth;
    private final String categoryId;
    private final String categoryName;
    private final long amountLimit;
    private final long spentAmount;
    private final long remainingAmount;
    private final int usageRate;

    public BudgetUsagePublicDto(String budgetId,
                                YearMonth targetMonth,
                                String categoryId,
                                String categoryName,
                                long amountLimit,
                                long spentAmount,
                                long remainingAmount,
                                int usageRate) {
        this.budgetId = budgetId;
        this.targetMonth = targetMonth;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.amountLimit = amountLimit;
        this.spentAmount = spentAmount;
        this.remainingAmount = remainingAmount;
        this.usageRate = usageRate;
    }

    public String getBudgetId() {
        return budgetId;
    }

    public YearMonth getTargetMonth() {
        return targetMonth;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public long getAmountLimit() {
        return amountLimit;
    }

    public long getSpentAmount() {
        return spentAmount;
    }

    public long getRemainingAmount() {
        return remainingAmount;
    }

    public int getUsageRate() {
        return usageRate;
    }
}
