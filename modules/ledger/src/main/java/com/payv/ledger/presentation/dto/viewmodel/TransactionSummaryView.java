package com.payv.ledger.presentation.dto.viewmodel;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class TransactionSummaryView {

    private final String transactionId;
    private final String transactionType;
    private final long amount;
    private final LocalDate transactionDate;

    private final String assetId;
    private final String assetName;             // ACL로 주입 가능

    private final String categoryIdLevel1;
    private final String categoryNameLevel1;    // ACL로 주입

    private final String memo;
    private final String sourceType;
    private final String sourceDisplayName;

}
