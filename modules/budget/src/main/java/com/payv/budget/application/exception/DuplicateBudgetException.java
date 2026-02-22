package com.payv.budget.application.exception;

import com.payv.common.error.ConflictException;

public class DuplicateBudgetException extends ConflictException {

    public DuplicateBudgetException() {
        super("BUDGET-409", "duplicate budget exists for month/category");
    }
}
