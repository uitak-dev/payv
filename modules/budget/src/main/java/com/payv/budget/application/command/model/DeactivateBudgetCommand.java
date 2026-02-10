package com.payv.budget.application.command.model;

import com.payv.budget.domain.model.BudgetId;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DeactivateBudgetCommand {
    private final BudgetId budgetId;
}
