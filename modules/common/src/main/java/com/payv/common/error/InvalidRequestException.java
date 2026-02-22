package com.payv.common.error;

public class InvalidRequestException extends BadRequestException {

    public InvalidRequestException(String message) {
        super("COMMON-400", message);
    }
}
