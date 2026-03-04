package com.payv.budget.application.query.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
public class BudgetView implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String budgetId;
    private final String targetMonth;
    private final String categoryId;
    private final String categoryName;
    private final long amountLimit;
    private final String memo;
    private final long spentAmount;
    private final long remainingAmount;
    private final int usageRate;
}
