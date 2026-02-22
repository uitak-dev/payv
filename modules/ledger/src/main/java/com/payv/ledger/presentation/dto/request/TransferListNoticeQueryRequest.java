package com.payv.ledger.presentation.dto.request;

import lombok.Data;

@Data
public final class TransferListNoticeQueryRequest {
    private String created;
    private String updated;
    private String deleted;
    private String error;
}
