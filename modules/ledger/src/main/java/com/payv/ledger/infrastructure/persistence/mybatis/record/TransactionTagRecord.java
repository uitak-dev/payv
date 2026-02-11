package com.payv.ledger.infrastructure.persistence.mybatis.record;

import lombok.Builder;
import lombok.Getter;

@Getter
public final class TransactionTagRecord {

    private String transactionId;
    private String tagId;
    private String ownerUserId;

    @Builder
    public TransactionTagRecord(String transactionId, String tagId, String ownerUserId) {
        this.transactionId = transactionId;
        this.tagId = tagId;
        this.ownerUserId = ownerUserId;
    }

    public TransactionTagRecord(String transactionId, String tagId) {
        this(transactionId, tagId, null);
    }

}
