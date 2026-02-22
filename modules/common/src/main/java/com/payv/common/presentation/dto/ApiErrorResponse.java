package com.payv.common.presentation.dto;

import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
public final class ApiErrorResponse {

    private final boolean success;
    private final String code;
    private final String message;
    private final int status;
    private final String path;
    private final String timestamp;

    private ApiErrorResponse(boolean success,
                             String code,
                             String message,
                             int status,
                             String path,
                             String timestamp) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.status = status;
        this.path = path;
        this.timestamp = timestamp;
    }

    public static ApiErrorResponse of(String code, String message, int status, String path) {
        return new ApiErrorResponse(
                false,
                code,
                message,
                status,
                path,
                OffsetDateTime.now().toString()
        );
    }
}
