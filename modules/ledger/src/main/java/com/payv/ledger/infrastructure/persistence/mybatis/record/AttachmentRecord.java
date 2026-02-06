package com.payv.ledger.infrastructure.persistence.mybatis.record;

import com.payv.ledger.domain.model.Attachment;
import com.payv.ledger.domain.model.AttachmentId;
import com.payv.ledger.domain.model.TransactionId;
import lombok.Builder;
import lombok.Getter;

@Getter
public final class AttachmentRecord {

    private String attachmentId;
    private String transactionId;
    private String ownerUserId;

    private String status;      // UPLOADING, STORED, FAILED

    private String uploadFileName;
    private String storedFileName;
    private String storagePath;

    private String stagingPath;
    private String stagingFileName;

    private String contentType;
    private long sizeBytes;

    private String failureReason;

    @Builder
    public AttachmentRecord(String attachmentId, String transactionId, String ownerUserId,
                            String status, String uploadFileName, String storedFileName,
                            String storagePath, String stagingPath, String stagingFileName,
                            String contentType, long sizeBytes, String failureReason) {

        this.attachmentId = attachmentId;
        this.transactionId = transactionId;
        this.ownerUserId = ownerUserId;
        this.status = status;
        this.uploadFileName = uploadFileName;
        this.storedFileName = storedFileName;
        this.storagePath = storagePath;
        this.stagingPath = stagingPath;
        this.stagingFileName = stagingFileName;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.failureReason = failureReason;
    }

    public static AttachmentRecord toRecord(Attachment attachment) {
        return AttachmentRecord.builder()
                .attachmentId(attachment.getId().getValue())
                .transactionId(attachment.getTransactionId().getValue())
                .ownerUserId(attachment.getOwnerUserId())
                .uploadFileName(attachment.getUploadFileName())
                .storedFileName(attachment.getStoredFileName())
                .storagePath(attachment.getStoragePath())
                .stagingPath(attachment.getStagingPath())
                .stagingFileName(attachment.getStagingFileName())
                .contentType(attachment.getContentType())
                .sizeBytes(attachment.getSizeBytes())
                .status(attachment.getStatus().toString())
                .failureReason(attachment.getFailureReason())
                .build();
    }

    public Attachment toEntity() {
        return Attachment.builder()
                .id(AttachmentId.of(attachmentId))
                .transactionId(TransactionId.of(transactionId))
                .ownerUserId(ownerUserId)
                .uploadFileName(uploadFileName)
                .storedFileName(storedFileName)
                .storagePath(storagePath)
                .stagingPath(stagingPath)
                .stagingFileName(stagingFileName)
                .contentType(contentType)
                .sizeBytes(sizeBytes)
                .status(Attachment.Status.valueOf(status))
                .failureReason(failureReason)
                .build();
    }
}
