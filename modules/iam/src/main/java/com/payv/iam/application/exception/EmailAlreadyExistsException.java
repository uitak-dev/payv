package com.payv.iam.application.exception;

import com.payv.common.error.ConflictException;

public class EmailAlreadyExistsException extends ConflictException {

    public EmailAlreadyExistsException() {
        super("IAM-409", "email already exists");
    }
}
