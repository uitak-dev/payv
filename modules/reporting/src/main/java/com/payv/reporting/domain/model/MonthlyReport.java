package com.payv.reporting.domain.model;

public final class MonthlyReport {

    private final long totalExpense;
    private final long totalIncome;
    private final long budgetLimit;

    private MonthlyReport(long totalExpense, long totalIncome, long budgetLimit) {
        this.totalExpense = totalExpense;
        this.totalIncome = totalIncome;
        this.budgetLimit = budgetLimit;
    }

    public static MonthlyReport of(long totalExpense, long totalIncome, Long budgetLimit) {
        return new MonthlyReport(totalExpense, totalIncome, budgetLimit == null ? 0L : budgetLimit);
    }

    public long netAmount() {
        return totalIncome - totalExpense;
    }

    public int budgetUsageRate() {
        if (budgetLimit <= 0L) return 0;
        return (int) ((totalExpense * 100.0d) / budgetLimit);
    }

    public int percentageOfExpense(long amount) {
        if (amount <= 0L || totalExpense <= 0L) return 0;
        return (int) ((amount * 100.0d) / totalExpense);
    }
}
