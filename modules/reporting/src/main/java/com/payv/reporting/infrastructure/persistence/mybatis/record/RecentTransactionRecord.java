package com.payv.reporting.infrastructure.persistence.mybatis.record;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class RecentTransactionRecord {
    private String transactionId;
    private String transactionType;
    private long amount;
    private LocalDate transactionDate;
    private String assetId;
    private String categoryIdLevel1;
    private String memo;
}
