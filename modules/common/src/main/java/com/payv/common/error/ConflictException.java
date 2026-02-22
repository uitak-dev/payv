package com.payv.common.error;

import org.springframework.http.HttpStatus;

public class ConflictException extends PayvException {

    public ConflictException(String errorCode, String message) {
        super(HttpStatus.CONFLICT, errorCode, message);
    }
}
