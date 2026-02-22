package com.payv.ledger.application.exception;

import com.payv.common.error.BadRequestException;

public class AttachmentStorageValidationException extends BadRequestException {

    public AttachmentStorageValidationException(String message) {
        super("LEDGER-STORAGE-400", message);
    }
}
