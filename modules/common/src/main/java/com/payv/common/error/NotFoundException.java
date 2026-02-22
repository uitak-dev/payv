package com.payv.common.error;

import org.springframework.http.HttpStatus;

public class NotFoundException extends PayvException {

    public NotFoundException(String errorCode, String message) {
        super(HttpStatus.NOT_FOUND, errorCode, message);
    }
}
