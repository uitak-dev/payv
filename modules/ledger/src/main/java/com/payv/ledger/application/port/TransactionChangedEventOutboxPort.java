package com.payv.ledger.application.port;

import com.payv.common.event.ledger.LedgerTransactionChangedEvent;

public interface TransactionChangedEventOutboxPort {

    void enqueue(LedgerTransactionChangedEvent event);
}

