package com.payv.ledger.application.port;

import com.payv.ledger.domain.model.AttachmentId;
import com.payv.ledger.domain.model.TransactionId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

public interface AttachmentStoragePort {

    StoragePlan plan(String ownerUserId, TransactionId transactionId, AttachmentId attachmentId,
                     String uploadFileName, String contentType);

    void saveToStaging(StoragePlan plan, MultipartFile file);

    void moveStagingToFinal(StoragePlan plan);

    void deleteStagingQuietly(StoragePlan plan);

    void deleteFinalQuietly(StoragePlan plan);

    @Getter
    @AllArgsConstructor
    class StoragePlan {
        private final String uploadFileName;
        private final String contentType;
        private final long sizeBytes;

        private final String storagePath;
        private final String storedFileName;

        private final String stagingPath;
        private final String stagingFileName;

    }
}
