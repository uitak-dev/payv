package com.payv.common.error;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends PayvException {

    public ForbiddenException(String errorCode, String message) {
        super(HttpStatus.FORBIDDEN, errorCode, message);
    }
}
