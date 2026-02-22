package com.payv.iam.application.exception;

import com.payv.common.error.ForbiddenException;

public class OwnershipDeniedException extends ForbiddenException {

    public OwnershipDeniedException() {
        super("IAM-403", "access denied: owner mismatch");
    }
}
