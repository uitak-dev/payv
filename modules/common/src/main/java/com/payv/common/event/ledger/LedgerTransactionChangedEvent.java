package com.payv.common.event.ledger;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerTransactionChangedEvent implements Serializable {

    private String ownerUserId;
    private LedgerTransactionChangeType changeType;
    private LedgerTransactionSnapshot before;
    private LedgerTransactionSnapshot after;
    private OffsetDateTime occurredAt;

    public Set<YearMonth> affectedExpenseMonths() {
        Set<YearMonth> months = new LinkedHashSet<>();
        if (before != null && before.isExpense() && before.getTransactionDate() != null) {
            months.add(YearMonth.from(before.getTransactionDate()));
        }
        if (after != null && after.isExpense() && after.getTransactionDate() != null) {
            months.add(YearMonth.from(after.getTransactionDate()));
        }
        return months;
    }

    public boolean isFixedExpenseAutoCreated() {
        return changeType == LedgerTransactionChangeType.CREATED
                && after != null
                && after.isExpense()
                && after.isFixedCostAuto();
    }
}
