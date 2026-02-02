package com.payv.ledger.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

@Getter
public final class Attachment {

    private final AttachmentId id;
    private final TransactionId transactionId;
    private final String ownerUserId;

    private final String uploadFileName;
    private final String storedFileName;
    private final String storagePath;

    private final String stagingPath;
    private final String stagingFileName;

    private final String contentType;
    private final long sizeBytes;

    private Status status;
    private String failureReason; // nullable

    @Builder
    private Attachment(AttachmentId id, TransactionId transactionId, String ownerUserId,
                       String uploadFileName, String storedFileName, String storagePath,
                       String stagingPath, String stagingFileName,
                       String contentType, long sizeBytes,
                       Status status, String failureReason) {

        this.id = id;
        this.transactionId = transactionId;
        this.ownerUserId = ownerUserId;
        this.uploadFileName = uploadFileName;
        this.storedFileName = storedFileName;
        this.storagePath = storagePath;
        this.stagingPath = stagingPath;
        this.stagingFileName = stagingFileName;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.status = status;
        this.failureReason = failureReason;
    }

    public static Attachment createForUpload(AttachmentId id, TransactionId transactionId, String ownerUserId,
                                    String uploadFileName, String storedFileName, String storagePath,
                                    String stagingPath, String stagingFileName,
                                    String contentType, long sizeBytes,
                                    Status status, String failureReason) {

        if (sizeBytes <= 0) throw new IllegalArgumentException("sizeBytes must be positive");

        return Attachment.builder()
                .id(id)
                .transactionId(transactionId)
                .ownerUserId(ownerUserId)
                .uploadFileName(uploadFileName)
                .storedFileName(storedFileName)
                .storagePath(storagePath)
                .stagingPath(stagingPath)
                .stagingFileName(stagingFileName)
                .contentType(contentType)
                .sizeBytes(sizeBytes)
                .status(status)
                .failureReason(failureReason)
                .build();
    }

    public static Attachment of(AttachmentId id, TransactionId transactionId, String ownerUserId,
                                String uploadFileName, String storedFileName, String storagePath,
                                String stagingPath, String stagingFileName,
                                String contentType, long sizeBytes,
                                Status status, String failureReason) {

        return Attachment.builder()
                .id(id)
                .transactionId(transactionId)
                .ownerUserId(ownerUserId)
                .uploadFileName(uploadFileName)
                .storedFileName(storedFileName)
                .storagePath(storagePath)
                .stagingPath(stagingPath)
                .stagingFileName(stagingFileName)
                .contentType(contentType)
                .sizeBytes(sizeBytes)
                .status(status)
                .failureReason(failureReason)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attachment that = (Attachment) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    public void markFailed(String reason) {
        this.status = Status.FAILED;
        this.failureReason = reason;
    }

    public void markStored() {
        this.status = Status.STORED;
        this.failureReason = null;
    }

    public enum Status {
        UPLOADING, STORED, FAILED
    }
}
