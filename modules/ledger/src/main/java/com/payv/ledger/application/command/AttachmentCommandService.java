package com.payv.ledger.application.command;

import com.payv.ledger.application.exception.AttachmentLimitExceededException;
import com.payv.ledger.application.exception.AttachmentNotFoundException;
import com.payv.ledger.application.port.AttachmentStoragePort;
import com.payv.ledger.domain.model.Attachment;
import com.payv.ledger.domain.model.AttachmentId;
import com.payv.ledger.domain.model.TransactionId;
import com.payv.ledger.domain.repository.AttachmentRepository;
import com.payv.ledger.domain.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AttachmentCommandService {

    private static final int MAX_ATTACHMENTS = 2;

    private final AttachmentRepository attachmentRepository;
    private final AttachmentStoragePort storagePort;
    private final TransactionTemplate txTemplate;

    @Transactional
    public AttachmentId upload(TransactionId transactionId, String ownerUserId, MultipartFile file) {

        // 1) 개수 제한(UPLOADING+STORED)
        int activeCount = attachmentRepository.countActiveByTransactionId(transactionId, ownerUserId);
        if (activeCount >= MAX_ATTACHMENTS) {
            throw new AttachmentLimitExceededException();
        }

        // 2) 식별자/파일명/경로 계획 수립
        AttachmentId attachmentId = AttachmentId.generate();

        String uploadFileName = safeDisplayName(file.getOriginalFilename());
        String contentType = (file.getContentType() != null) ? file.getContentType() : "application/octet-stream";
        long sizeBytes = file.getSize();

        AttachmentStoragePort.StoragePlan plan =
                storagePort.plan(ownerUserId, transactionId, attachmentId, uploadFileName, contentType);

        // 3) staging 저장 (DB 커밋 전)
        storagePort.saveToStaging(plan, file);

        // 4) meta insert(UPLOADING)
        Attachment uploading = Attachment.createForUpload(
                attachmentId,
                transactionId,
                ownerUserId,
                uploadFileName,
                plan.getStoredFileName(),
                plan.getStoragePath(),
                plan.getStagingPath(),
                plan.getStagingFileName(),
                contentType,
                sizeBytes,
                Attachment.Status.UPLOADING,
                null
        );
        attachmentRepository.insertUploading(uploading);

        // 5) 커밋 이후 finalize(move) + status update (새 트랜잭션)
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                txTemplate.execute(status -> {
                    try {
                        storagePort.moveStagingToFinal(plan);
                        attachmentRepository.markStored(attachmentId, ownerUserId);
                    } catch (Exception ex) {
                        attachmentRepository.markFailed(attachmentId, ownerUserId, shortReason(ex));
                        // 정책: staging 남김(재시도/운영점검) or 삭제
                        // storagePort.deleteStagingQuietly(plan);
                    }
                    return null;
                });
            }

            @Override
            public void afterCompletion(int status) {
                if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
                    storagePort.deleteStagingQuietly(plan);
                }
            }
        });

        return attachmentId;
    }

    @Transactional
    public void delete(AttachmentId attachmentId, String ownerUserId) {
        Attachment attachment = attachmentRepository.findById(attachmentId, ownerUserId)
                .orElseThrow(AttachmentNotFoundException::new);

        AttachmentStoragePort.StoragePlan plan = new AttachmentStoragePort.StoragePlan(
                attachment.getUploadFileName(),
                attachment.getContentType(),
                attachment.getSizeBytes(),
                attachment.getStoragePath(),
                attachment.getStoredFileName(),
                attachment.getStagingPath(),
                attachment.getStagingFileName()
        );

        if (attachment.getStatus() == Attachment.Status.UPLOADING) {
            storagePort.deleteStagingQuietly(plan);
        } else if (attachment.getStatus() == Attachment.Status.STORED) {
            storagePort.deleteFinalQuietly(plan);
        } else {
            storagePort.deleteStagingQuietly(plan);
            storagePort.deleteFinalQuietly(plan);
        }

        attachmentRepository.deleteById(attachmentId, ownerUserId);
    }

    private String safeDisplayName(String original) {
        if (original == null) return "unknown";
        String s = original.replace("\\", "/");
        int idx = s.lastIndexOf('/');
        String name = (idx >= 0) ? s.substring(idx + 1) : s;
        if (name.length() > 150) name = name.substring(0, 150);
        return name;
    }

    private String shortReason(Exception ex) {
        String msg = ex.getMessage();
        if (msg == null) return ex.getClass().getSimpleName();
        return msg.length() > 200 ? msg.substring(0, 200) : msg;
    }
}
