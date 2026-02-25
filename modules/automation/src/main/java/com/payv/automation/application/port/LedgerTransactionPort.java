package com.payv.automation.application.port;

import com.payv.automation.domain.model.FixedExpenseExecution;

public interface LedgerTransactionPort {

    String createFixedExpenseAutoTransaction(FixedExpenseExecution execution, String ownerUserId);
}
