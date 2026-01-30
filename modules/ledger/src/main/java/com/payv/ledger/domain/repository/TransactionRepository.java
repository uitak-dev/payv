package com.payv.ledger.domain.repository;

import com.payv.ledger.domain.model.Transaction;
import com.payv.ledger.domain.model.TransactionId;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository {

    void save(Transaction transaction);

    Optional<Transaction> findById(TransactionId id, String ownerUserId);

}
