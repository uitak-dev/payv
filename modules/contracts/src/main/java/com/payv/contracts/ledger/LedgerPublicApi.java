package com.payv.contracts.ledger;

import com.payv.contracts.ledger.dto.CreateFixedExpenseAutoTransactionPublicRequest;

import java.time.LocalDate;

public interface LedgerPublicApi {

    long sumExpenseAmount(String ownerUserId,
                          LocalDate from,
                          LocalDate to,
                          String categoryIdLevel1,
                          String categoryIdLevel2);

    String createFixedExpenseAutoTransaction(String ownerUserId,
                                             CreateFixedExpenseAutoTransactionPublicRequest request);
}
