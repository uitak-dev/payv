package com.payv.ledger.application.exception;

import com.payv.common.error.NotFoundException;

public class AttachmentNotFoundException extends NotFoundException {

    public AttachmentNotFoundException() {
        super("LEDGER-ATTACHMENT-404", "attachment not found");
    }
}
