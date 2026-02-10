package com.payv.ledger.presentation.dto.request;

import lombok.Data;

@Data
public final class TransactionDetailNoticeQueryRequest {
    private String created;
    private String updated;
    private String attachmentUploaded;
    private String attachmentDeleted;
    private String error;
}
