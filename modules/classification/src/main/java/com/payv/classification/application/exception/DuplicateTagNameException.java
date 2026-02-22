package com.payv.classification.application.exception;

import com.payv.common.error.ConflictException;

public class DuplicateTagNameException extends ConflictException {

    public DuplicateTagNameException() {
        super("CLASSIFICATION-TAG-409", "duplicate tag name");
    }
}
