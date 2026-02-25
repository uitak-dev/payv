package com.payv.automation.application.exception;

import com.payv.common.error.NotFoundException;

public class FixedExpenseNotFoundException extends NotFoundException {

    public FixedExpenseNotFoundException() {
        super("AUTOMATION-FIXED-EXPENSE-404", "fixed expense not found");
    }
}
