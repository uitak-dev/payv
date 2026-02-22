package com.payv.ledger.application.exception;

import com.payv.common.error.PayvException;
import org.springframework.http.HttpStatus;

public class AttachmentStorageFailureException extends PayvException {

    public AttachmentStorageFailureException(String message, Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "LEDGER-STORAGE-500", message, cause);
    }

    public AttachmentStorageFailureException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "LEDGER-STORAGE-500", message);
    }
}
