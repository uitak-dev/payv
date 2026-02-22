package com.payv.ledger.application.exception;

import com.payv.common.error.ConflictException;

public class AttachmentLimitExceededException extends ConflictException {

    public AttachmentLimitExceededException() {
        super("LEDGER-ATTACHMENT-409", "attachment limit exceeded");
    }
}
