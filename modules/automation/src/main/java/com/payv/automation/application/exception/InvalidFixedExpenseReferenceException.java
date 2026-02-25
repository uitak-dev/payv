package com.payv.automation.application.exception;

import com.payv.common.error.BadRequestException;

public class InvalidFixedExpenseReferenceException extends BadRequestException {

    public InvalidFixedExpenseReferenceException(String message) {
        super("AUTOMATION-FIXED-EXPENSE-400", message);
    }
}
