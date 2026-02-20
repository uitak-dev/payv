package com.payv.reporting.application.query.model;

import com.payv.reporting.application.query.ReportingQueryService;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Getter
@AllArgsConstructor
public class HomeDashboardView {

    private final YearMonth month;
    private final LocalDate monthStart;
    private final LocalDate monthEnd;
    private final long incomeAmount;
    private final long expenseAmount;
    private final long netAmount;
    private final boolean hasOverallBudget;
    private final long remainingBudget;
    private final int budgetUsageRate;
    private final List<RecentTransactionView> recentTransactions;
}
