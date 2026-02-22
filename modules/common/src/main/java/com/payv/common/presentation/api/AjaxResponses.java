package com.payv.common.presentation.api;

import org.springframework.http.ResponseEntity;

import java.util.LinkedHashMap;
import java.util.Map;

public final class AjaxResponses {

    private AjaxResponses() {
    }

    public static ResponseEntity<Map<String, Object>> okRedirect(String redirectPath) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("redirectUrl", redirectPath);
        return ResponseEntity.ok(body);
    }

    public static ResponseEntity<Map<String, Object>> badRequest(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("message", message == null ? "request failed" : message);
        return ResponseEntity.badRequest().body(body);
    }
}
