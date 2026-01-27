package com.payv.ledger.application.query;

import com.payv.ledger.domain.model.Transaction;
import com.payv.ledger.domain.model.TransactionId;
import com.payv.ledger.domain.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionQueryService {

    private final TransactionRepository transactionRepository;

    public Transaction findById(String transactionId) {
        return transactionRepository
                .findById(TransactionId.of(transactionId))
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));
    }
}
