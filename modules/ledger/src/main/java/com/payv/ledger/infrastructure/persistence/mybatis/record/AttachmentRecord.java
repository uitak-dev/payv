package com.payv.ledger.infrastructure.persistence.mybatis.record;

import com.payv.ledger.domain.model.Attachment;
import com.payv.ledger.domain.model.AttachmentId;
import com.payv.ledger.domain.model.TransactionId;
import lombok.Builder;
import lombok.Getter;

@Getter
public final class AttachmentRecord {

    private String id;
    private String transactionId;
    private String uploadFileName;
    private String storedFileName;
    private String storagePath;
    private String contentType;
    private long sizeBytes;

    @Builder
    private AttachmentRecord(String id, String transactionId, String uploadFileName,
                             String storedFileName, String storagePath, String contentType, long sizeBytes) {
        this.id = id;
        this.transactionId = transactionId;
        this.uploadFileName = uploadFileName;
        this.storedFileName = storedFileName;
        this.storagePath = storagePath;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
    }

    public static AttachmentRecord toRecord(Attachment attachment) {
        return AttachmentRecord.builder()
                .id(attachment.getId().getValue())
                .transactionId(attachment.getTransactionId().getValue())
                .uploadFileName(attachment.getUploadFileName())
                .storedFileName(attachment.getStoredFileName())
                .storagePath(attachment.getStoragePath())
                .contentType(attachment.getContentType())
                .sizeBytes(attachment.getSizeBytes())
                .build();
    }

    public Attachment toEntity() {
        return Attachment.builder()
                .id(AttachmentId.of(id))
                .transactionId(TransactionId.of(transactionId))
                .uploadFileName(uploadFileName)
                .storedFileName(storedFileName)
                .storagePath(storagePath)
                .contentType(contentType)
                .sizeBytes(sizeBytes)
                .build();
    }
}
