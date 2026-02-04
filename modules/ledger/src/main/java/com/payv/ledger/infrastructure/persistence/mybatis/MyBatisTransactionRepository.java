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

import java.util.List;
import java.util.Optional;
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
        boolean isExist = transactionMapper.existsById(transaction.getId().toString());
        if (isExist) {
            transactionMapper.update(record);
        } else {
            transactionMapper.insert(record);
        }

        // tags: replace 전략(간단/명확). 규모 커지면 diff로 최적화.
        transactionTagMapper.deleteByTransactionId(record.getTransactionId());
        if (!transaction.getTagIds().isEmpty()) {
            List<TransactionTagRecord> tags = transaction.getTagIds().stream()
                    .map(tagId -> new TransactionTagRecord(record.getTransactionId(), tagId))
                    .collect(Collectors.toList());
            transactionTagMapper.insertTags(tags);
        }

        // attachments: replace 전략(간단/명확). 규모 커지면 diff로 최적화.
        attachmentMapper.deleteByTransactionId(record.getTransactionId());
        if (!transaction.getAttachments().isEmpty()) {
            List<AttachmentRecord> atts = transaction.getAttachments().stream()
                    .map(AttachmentRecord::toRecord)
                    .collect(Collectors.toList());
            attachmentMapper.insertAttachments(atts);
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
