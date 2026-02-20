package com.payv.reporting.application.port.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OverallBudgetSnapshotDto {
    private final String budgetId;
    private final long amountLimit;
    private final long spentAmount;
    private final long remainingAmount;
    private final int usageRate;
}
