package com.payv.ledger.infrastructure.persistence.mybatis;

import com.payv.ledger.domain.model.Transaction;
import com.payv.ledger.domain.model.TransactionId;
import com.payv.ledger.infrastructure.persistence.mybatis.assembler.TransactionAssembler;
import com.payv.ledger.domain.repository.TransactionRepository;
import com.payv.ledger.infrastructure.persistence.mybatis.mapper.AttachmentMapper;
import com.payv.ledger.infrastructure.persistence.mybatis.mapper.TransactionMapper;
import com.payv.ledger.infrastructure.persistence.mybatis.mapper.TransactionTagMapper;
import com.payv.ledger.infrastructure.persistence.mybatis.record.AttachmentRecord;
import com.payv.ledger.infrastructure.persistence.mybatis.record.TransactionRecord;
import com.payv.ledger.infrastructure.persistence.mybatis.record.TransactionTagRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class MyBatisTransactionRepository implements TransactionRepository {

    private final TransactionMapper transactionMapper;
    private final TransactionTagMapper transactionTagMapper;
    private final AttachmentMapper attachmentMapper;

    @Override
    public void save(Transaction transaction) {

        // transaction
        TransactionRecord record = TransactionRecord.toRecord(transaction);
        transactionMapper.upsert(record);

        // tags: diff 전략
        List<TransactionTagRecord> existingTagRecords =
                transactionTagMapper.selectByTransactionId(record.getTransactionId());
        Set<String> existingTagIds = existingTagRecords.stream()
                .map(TransactionTagRecord::getTagId)
                .collect(Collectors.toSet());
        Set<String> newTagIds = new HashSet<>(transaction.getTagIds());

        List<String> tagIdsToDelete = new ArrayList<>();
        for (String existingTagId : existingTagIds) {
            if (!newTagIds.contains(existingTagId)) {
                tagIdsToDelete.add(existingTagId);
            }
        }
        if (!tagIdsToDelete.isEmpty()) {
            transactionTagMapper.deleteByTransactionIdAndTagIds(record.getTransactionId(), tagIdsToDelete);
        }

        List<TransactionTagRecord> tagRecordsToInsert = new ArrayList<>();
        for (String newTagId : newTagIds) {
            if (!existingTagIds.contains(newTagId)) {
                tagRecordsToInsert.add(new TransactionTagRecord(record.getTransactionId(), newTagId));
            }
        }
        if (!tagRecordsToInsert.isEmpty()) {
            transactionTagMapper.insertTags(tagRecordsToInsert);
        }
    }

    @Override
    public Optional<Transaction> findById(TransactionId id, String ownerUserId) {
        TransactionRecord record = transactionMapper.selectDetail(id.getValue(), ownerUserId);
        if (record == null) return Optional.empty();

        List<TransactionTagRecord> tags = transactionTagMapper.selectByTransactionId(record.getTransactionId());
        List<AttachmentRecord> atts = attachmentMapper.selectStoredByTransactionId(record.getTransactionId(), ownerUserId);

        return Optional.of(TransactionAssembler.toEntity(record, tags, atts));
    }

}
