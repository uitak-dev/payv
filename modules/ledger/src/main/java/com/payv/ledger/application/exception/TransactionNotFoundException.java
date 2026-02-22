package com.payv.ledger.application.exception;

import com.payv.common.error.NotFoundException;

public class TransactionNotFoundException extends NotFoundException {

    public TransactionNotFoundException() {
        super("LEDGER-TX-404", "transaction not found");
    }
}
