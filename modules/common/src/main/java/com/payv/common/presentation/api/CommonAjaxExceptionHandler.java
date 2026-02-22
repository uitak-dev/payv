package com.payv.common.presentation.api;

import com.payv.common.error.PayvException;
import com.payv.common.presentation.dto.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

@RestControllerAdvice(annotations = ResponseBody.class)
public class CommonAjaxExceptionHandler {

    @ExceptionHandler(PayvException.class)
    public ResponseEntity<ApiErrorResponse> handlePayvException(PayvException e, HttpServletRequest request) {
        return buildResponse(e.getStatus(), e.getErrorCode(), e.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(IllegalArgumentException e,
                                                                           HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "COMMON-400", e.getMessage(), request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalStateException(IllegalStateException e,
                                                                        HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "COMMON-409", e.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception e, HttpServletRequest request) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "COMMON-500",
                "internal server error",
                request
        );
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatus status,
                                                           String code,
                                                           String message,
                                                           HttpServletRequest request) {
        String path = request == null ? "" : request.getRequestURI();
        String safeMessage = message == null || message.trim().isEmpty() ? "request failed" : message;
        ApiErrorResponse body = ApiErrorResponse.of(code, safeMessage, status.value(), path);
        return ResponseEntity.status(status).body(body);
    }
}
