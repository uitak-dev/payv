package com.payv.ledger.application.exception;

import com.payv.common.error.NotFoundException;

public class TransferNotFoundException extends NotFoundException {

    public TransferNotFoundException() {
        super("LEDGER-TRANSFER-404", "transfer not found");
    }
}
