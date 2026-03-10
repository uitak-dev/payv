package com.payv.contracts.ledger.dto;

import java.time.LocalDate;

public class CreateFixedExpenseAutoTransactionPublicRequest {

    private final String fixedExpenseDefinitionId;
    private final long amount;
    private final LocalDate transactionDate;
    private final String assetId;
    private final String categoryIdLevel1;
    private final String categoryIdLevel2;
    private final String memo;

    public CreateFixedExpenseAutoTransactionPublicRequest(String fixedExpenseDefinitionId,
                                                          long amount,
                                                          LocalDate transactionDate,
                                                          String assetId,
                                                          String categoryIdLevel1,
                                                          String categoryIdLevel2,
                                                          String memo) {
        this.fixedExpenseDefinitionId = fixedExpenseDefinitionId;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.assetId = assetId;
        this.categoryIdLevel1 = categoryIdLevel1;
        this.categoryIdLevel2 = categoryIdLevel2;
        this.memo = memo;
    }

    public String getFixedExpenseDefinitionId() {
        return fixedExpenseDefinitionId;
    }

    public long getAmount() {
        return amount;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public String getAssetId() {
        return assetId;
    }

    public String getCategoryIdLevel1() {
        return categoryIdLevel1;
    }

    public String getCategoryIdLevel2() {
        return categoryIdLevel2;
    }

    public String getMemo() {
        return memo;
    }
}
