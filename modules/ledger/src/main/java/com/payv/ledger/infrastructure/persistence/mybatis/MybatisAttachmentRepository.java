package com.payv.ledger.infrastructure.persistence.mybatis;

import com.payv.ledger.domain.model.Attachment;
import com.payv.ledger.domain.model.AttachmentId;
import com.payv.ledger.domain.model.TransactionId;
import com.payv.ledger.domain.repository.AttachmentRepository;
import com.payv.ledger.infrastructure.persistence.mybatis.mapper.AttachmentMapper;
import com.payv.ledger.infrastructure.persistence.mybatis.record.AttachmentRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MybatisAttachmentRepository implements AttachmentRepository {

    private final AttachmentMapper mapper;

    @Override
    public int countActiveByTransactionId(TransactionId id, String ownerUserId) {
        return mapper.countActiveByTransactionId(id.getValue(), ownerUserId);
    }

    @Override
    public void insertUploading(Attachment attachment) {
        mapper.insertUploading(AttachmentRecord.toRecord(attachment));
    }

    @Override
    public List<Attachment> findStoredByTransactionId(TransactionId id, String ownerUserId) {
        List<AttachmentRecord> records = mapper.selectStoredByTransactionId(id.getValue(), ownerUserId);
        List<Attachment> result = new ArrayList<>(records.size());
        for (AttachmentRecord r : records) {
            result.add(r.toEntity());
        }
        return result;
    }

    @Override
    public Optional<Attachment> findById(AttachmentId id, String ownerUserId) {
        AttachmentRecord record = mapper.selectById(id.getValue(), ownerUserId);
        if (record == null) return Optional.empty();
        return Optional.of(record.toEntity());
    }

    @Override
    public void deleteById(AttachmentId id, String ownerUserId) {
        mapper.deleteById(id.getValue(), ownerUserId);
    }

    @Override
    public void markStored(AttachmentId id, String ownerUserId) {
        mapper.markStored(id.getValue(), ownerUserId);
    }

    @Override
    public void markFailed(AttachmentId id, String ownerUserId, String failureReason) {
        mapper.markFailed(id.getValue(), ownerUserId, failureReason);
    }
}
