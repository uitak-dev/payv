package com.payv.budget.application.exception;

import com.payv.common.error.BadRequestException;

public class InvalidBudgetCategoryException extends BadRequestException {

    public InvalidBudgetCategoryException() {
        super("BUDGET-400", "budget category must be active 1-depth category");
    }
}
