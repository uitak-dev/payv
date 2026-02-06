package com.payv.ledger.domain.repository;

import com.payv.ledger.domain.model.Attachment;
import com.payv.ledger.domain.model.AttachmentId;
import com.payv.ledger.domain.model.TransactionId;
import java.util.List;
import java.util.Optional;

public interface AttachmentRepository {

    // UPLOADING + STORED만 카운트 (실패는 제외)
    int countActiveByTransactionId(TransactionId id, String ownerUserId);
    List<Attachment> findStoredByTransactionId(TransactionId id, String ownerUserId);
    Optional<Attachment> findById(AttachmentId id, String ownerUserId);

    void insertUploading(Attachment attachment);
    void deleteById(AttachmentId id, String ownerUserId);

    void markStored(AttachmentId id, String ownerUserId);
    void markFailed(AttachmentId id, String ownerUserId, String failureReason);
}
