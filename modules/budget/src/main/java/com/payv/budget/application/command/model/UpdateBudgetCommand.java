package com.payv.budget.application.command.model;

import com.payv.budget.domain.model.BudgetId;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.YearMonth;

@Getter
@AllArgsConstructor
public class UpdateBudgetCommand {
    private final BudgetId budgetId;
    private final YearMonth targetMonth;
    private final long amountLimit;
    private final String categoryId;
    private final String memo;
}
