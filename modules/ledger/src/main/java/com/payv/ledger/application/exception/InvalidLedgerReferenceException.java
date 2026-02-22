package com.payv.ledger.application.exception;

import com.payv.common.error.BadRequestException;

public class InvalidLedgerReferenceException extends BadRequestException {

    public InvalidLedgerReferenceException(String message) {
        super("LEDGER-REFERENCE-400", message);
    }
}
