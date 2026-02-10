package com.payv.ledger.presentation.dto.request;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public final class TransactionListQueryRequest {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate from;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate to;

    private String assetId;
    private int page = 1;
    private int size = 20;

    public String normalizedAssetId() {
        if (assetId == null) return null;
        String trimmed = assetId.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
