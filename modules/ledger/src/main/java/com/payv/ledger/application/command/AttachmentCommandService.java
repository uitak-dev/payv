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
/**
 * 첨부파일 업로드/삭제 명령 서비스.
 * - 거래 첨부파일의 staging 저장, 메타데이터 저장, commit 후 최종 반영을 처리한다.
 * - 파일 시스템과 DB가 동시에 변경되는 시나리오에서 트랜잭션 경계를 분리 일관성과 복구 가능성을 높인다.
 */
public class AttachmentCommandService {

    private static final int MAX_ATTACHMENTS = 2;

    private final AttachmentRepository attachmentRepository;
    private final AttachmentStoragePort storagePort;
    private final TransactionTemplate txTemplate;

    /**
     * 거래 첨부파일을 업로드한다.
     *
     * Business logic:
     * - 최대 개수(2개) 제한 확인
     * - 파일을 staging에 먼저 저장
     * - DB에는 UPLOADING 상태로 insert
     * - DB 커밋 후 별도 트랜잭션에서 최종 경로로 move + STORED 갱신
     * - 커밋 실패 시 staging 정리
     *
     * @param transactionId 첨부 대상 거래 ID
     * @param ownerUserId 소유 사용자 ID
     * @param file 업로드 파일
     * @return 생성된 첨부파일 ID
     * @throws AttachmentLimitExceededException 첨부파일 개수 제한(2개)을 초과한 경우
     */
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

            // 현재 진행 중인 트랜잭션(Committed 또는 Rolled Back)이 완전히 끝난 직후, 실행.
            // `afterCommit()`보다 더 나중에 호출된다.
            @Override
            public void afterCompletion(int status) {
                if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
                    storagePort.deleteStagingQuietly(plan);
                }
            }
        });

        return attachmentId;
    }

    /**
     * 첨부파일을 삭제한다.
     *
     * @param attachmentId 삭제할 첨부파일 ID
     * @param ownerUserId 소유 사용자 ID
     * @throws AttachmentNotFoundException 첨부파일이 없거나 소유자가 일치하지 않는 경우
     */
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
