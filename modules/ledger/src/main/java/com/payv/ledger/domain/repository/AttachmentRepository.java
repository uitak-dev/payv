package com.payv.ledger.domain.repository;

import com.payv.ledger.domain.model.Attachment;
import com.payv.ledger.domain.model.AttachmentId;
import com.payv.ledger.domain.model.TransactionId;

public interface AttachmentRepository {

    int countByTransactionId(TransactionId id);
    void insertUploading(Attachment attachment);
    void markStored(AttachmentId id);
    void markFailed(AttachmentId id, String reason);
}
