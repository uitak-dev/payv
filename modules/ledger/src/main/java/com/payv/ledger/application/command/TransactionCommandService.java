package com.payv.ledger.application.command;

import com.payv.ledger.application.command.model.CreateTransactionCommand;
import com.payv.ledger.domain.model.Transaction;
import com.payv.ledger.domain.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionCommandService {

    private final TransactionRepository transactionRepository;

    public Transaction createTransaction(CreateTransactionCommand command) {
        Transaction transaction = Transaction.of(command.getTransactionDate(),
                command.getMemo());
        return transactionRepository.save(transaction);
    }
}
