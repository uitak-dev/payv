package com.payv.ledger.infrastructure.persistence.mybatis.record;

import lombok.Builder;
import lombok.Getter;

@Getter
public final class TransactionTagRecord {

    private String transactionId;
    private String tagId;

    @Builder
    public TransactionTagRecord(String transactionId, String tagId) {
        this.transactionId = transactionId;
        this.tagId = tagId;
    }

}
