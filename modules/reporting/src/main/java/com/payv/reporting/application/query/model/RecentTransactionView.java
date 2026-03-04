package com.payv.reporting.application.query.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class RecentTransactionView implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String transactionId;
    private final String transactionType;
    private final long amount;
    private final LocalDate transactionDate;
    private final String assetId;
    private final String assetName;
    private final String categoryIdLevel1;
    private final String categoryNameLevel1;
    private final String memo;
}
