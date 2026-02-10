package com.payv.ledger.presentation.dto.request;

import lombok.Data;

@Data
public final class TransactionListNoticeQueryRequest {
    private String created;
    private String updated;
    private String deleted;
    private String error;
}
