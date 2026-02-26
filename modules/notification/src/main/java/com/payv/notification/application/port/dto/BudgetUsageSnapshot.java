package com.payv.notification.application.port.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.YearMonth;

@Getter
@AllArgsConstructor
public class BudgetUsageSnapshot {

    private final String budgetId;
    private final YearMonth targetMonth;
    private final String categoryId;
    private final String categoryName;
    private final long amountLimit;
    private final long spentAmount;
    private final int usageRate;
}
