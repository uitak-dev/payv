package com.payv.ledger.infrastructure.persistence.mybatis.record;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TransactionRecord {

    private String id;
    private LocalDate transactionDate;
    private String memo;

    @Builder
    public TransactionRecord(String transactionId, LocalDate transactionDate, String memo) {
        this.id = transactionId;
        this.transactionDate = transactionDate;
        this.memo = memo;
    }
}
