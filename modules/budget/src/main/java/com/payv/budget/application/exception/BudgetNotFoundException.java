package com.payv.budget.application.exception;

import com.payv.common.error.NotFoundException;

public class BudgetNotFoundException extends NotFoundException {

    public BudgetNotFoundException() {
        super("BUDGET-404", "budget not found");
    }
}
