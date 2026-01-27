package com.payv.ledger.infrastructure.persistence.mybatis;

import com.payv.ledger.domain.model.Transaction;
import com.payv.ledger.domain.model.TransactionId;
import com.payv.ledger.domain.repository.TransactionRepository;
import com.payv.ledger.infrastructure.persistence.mybatis.mapper.TransactionMapper;
import com.payv.ledger.infrastructure.persistence.mybatis.record.TransactionRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MyBatisTransactionRepository implements TransactionRepository {

    private final TransactionMapper mapper;

    @Override
    public Transaction save(Transaction transaction) {
        TransactionRecord record = toRecord(transaction);
        boolean isExist = mapper.existsById(transaction.getId().toString());
        if (isExist) {
            mapper.update(record);
        } else {
            mapper.insert(record);
        }
        return transaction;
    }

    @Override
    public Optional<Transaction> findById(TransactionId transactionId) {
        TransactionRecord record = mapper.selectById(transactionId.toString());
        return Optional.ofNullable(record).map(this::toEntity);
    }

    private TransactionRecord toRecord(Transaction transaction) {
        return TransactionRecord.builder()
                .transactionId(transaction.getId().toString())
                .transactionDate(transaction.getTransactionDate())
                .memo(transaction.getMemo())
                .build();
    }

    private Transaction toEntity(TransactionRecord record) {
        return Transaction.of(TransactionId.of(record.getId()),
                record.getTransactionDate(),
                record.getMemo());
    }
}
