package com.payv.reporting.application.query.model;

import com.payv.reporting.application.query.ReportingQueryService;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.YearMonth;
import java.util.List;

@Getter
@AllArgsConstructor
public class MonthlyReportView {

    private final YearMonth month;
    private final long totalExpense;
    private final long totalIncome;
    private final long netAmount;
    private final long overallBudgetLimit;
    private final long overallBudgetSpent;
    private final long overallBudgetRemaining;
    private final int budgetUsageRate;
    private final List<BreakdownView> assetExpenseSummary;
    private final List<BreakdownView> categoryExpenseSummary;
    private final List<TagSummaryView> tagExpenseSummary;
}
