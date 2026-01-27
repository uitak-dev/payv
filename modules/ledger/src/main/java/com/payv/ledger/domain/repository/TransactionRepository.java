package com.payv.ledger.domain.repository;

import com.payv.ledger.domain.model.Transaction;
import com.payv.ledger.domain.model.TransactionId;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository {

    Transaction save(Transaction transaction);

    Optional<Transaction> findById(TransactionId transactionId);

}
