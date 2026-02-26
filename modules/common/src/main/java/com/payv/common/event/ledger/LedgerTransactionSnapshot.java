package com.payv.common.event.ledger;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerTransactionSnapshot implements Serializable {

    private String transactionId;
    private String ownerUserId;
    private String transactionType;
    private LocalDate transactionDate;
    private long amount;
    private String sourceType;
    private String sourceReference;

    public boolean isExpense() {
        return "EXPENSE".equalsIgnoreCase(transactionType);
    }

    public boolean isFixedCostAuto() {
        return "FIXED_COST_AUTO".equalsIgnoreCase(sourceType);
    }
}
