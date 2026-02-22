package com.payv.ledger.presentation.dto.request;

import lombok.Data;

@Data
public final class TransferDetailNoticeQueryRequest {
    private String created;
    private String updated;
    private String error;
}
