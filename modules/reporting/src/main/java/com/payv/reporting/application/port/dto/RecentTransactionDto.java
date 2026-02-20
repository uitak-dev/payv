package com.payv.reporting.application.port.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class RecentTransactionDto {
    private final String transactionId;
    private final String transactionType;
    private final long amount;
    private final LocalDate transactionDate;
    private final String assetId;
    private final String categoryIdLevel1;
    private final String memo;
}
