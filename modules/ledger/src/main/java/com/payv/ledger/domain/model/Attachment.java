package com.payv.ledger.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

@Getter
public final class Attachment {

    private final AttachmentId id;
    private final TransactionId transactionId;
    private final String uploadFileName;
    private final String storedFileName;
    private final String storagePath;   // File = baseDir + storagePath + storedFileName + contentType
    private final String contentType;
    private final long sizeBytes;

    @Builder
    private Attachment(AttachmentId id, TransactionId transactionId,
                       String uploadFileName, String storedFileName,
                       String storagePath, String contentType, long sizeBytes) {
        this.id = id;
        this.transactionId = transactionId;
        this.uploadFileName = uploadFileName;
        this.storedFileName = storedFileName;
        this.storagePath = storagePath;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
    }

    public static Attachment create(String uploadFileName, TransactionId transactionId,
                                    String storagePath, String contentType, long sizeBytes) {
        return Attachment.builder()
                .id(AttachmentId.generate())
                .transactionId(transactionId)
                .uploadFileName(uploadFileName)
                .storedFileName(UUID.randomUUID().toString())
                .storagePath(storagePath)
                .contentType(contentType)
                .sizeBytes(sizeBytes)
                .build();
    }

    public static Attachment of(AttachmentId id, TransactionId transactionId,
                                String uploadFileName, String storedFileName,
                                String storagePath, String contentType, long sizeBytes) {
        return Attachment.builder()
                .id(id)
                .transactionId(transactionId)
                .uploadFileName(uploadFileName)
                .storedFileName(storedFileName)
                .storagePath(storagePath)
                .contentType(contentType)
                .sizeBytes(sizeBytes)
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
}
