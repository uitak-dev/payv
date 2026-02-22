package com.payv.ledger.application.exception;

import com.payv.common.error.NotFoundException;

public class AttachmentBinaryNotFoundException extends NotFoundException {

    public AttachmentBinaryNotFoundException() {
        super("LEDGER-STORAGE-404", "attachment file not found");
    }
}
