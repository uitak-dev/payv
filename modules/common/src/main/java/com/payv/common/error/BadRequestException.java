package com.payv.common.error;

import org.springframework.http.HttpStatus;

public class BadRequestException extends PayvException {

    public BadRequestException(String errorCode, String message) {
        super(HttpStatus.BAD_REQUEST, errorCode, message);
    }
}
