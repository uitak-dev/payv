package com.payv.ledger.domain.repository;

import com.payv.ledger.domain.model.Attachment;
import com.payv.ledger.domain.model.AttachmentId;
import com.payv.ledger.domain.model.TransactionId;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AttachmentRepository {

    // UPLOADING + STORED만 카운트 (실패는 제외)
    int countActiveByTransactionId(TransactionId id, String ownerUserId);
    void deleteByTransactionId(TransactionId id);
    void insertUploading(Attachment attachment);
    List<Attachment> findStoredByTransactionId(TransactionId id, String ownerUserId);

    void markStored(AttachmentId id, String ownerUserId);
    void markFailed(AttachmentId id, String ownerUserId, String failureReason);
}
