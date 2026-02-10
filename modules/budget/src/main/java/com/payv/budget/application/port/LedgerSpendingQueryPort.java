package com.payv.budget.application.port;

import java.time.LocalDate;

public interface LedgerSpendingQueryPort {
    long sumExpenseAmount(String ownerUserId, LocalDate from, LocalDate to, String categoryIdLevel1);
}
