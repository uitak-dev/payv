package com.payv.ledger.infrastructure.adapter;

import com.payv.ledger.application.port.AttachmentStoragePort;
import com.payv.ledger.domain.model.AttachmentId;
import com.payv.ledger.domain.model.TransactionId;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Locale;
import java.util.UUID;

@Component
public class LocalFsAttachmentStorageAdapter implements AttachmentStoragePort {

    private final Path baseDir;     // final root
    private final Path stagingRoot; // staging root (baseDir/_staging)

    public LocalFsAttachmentStorageAdapter(
            @Value("${payv.storage.base-dir:/var/payv/uploads}") String baseDir) {
        this.baseDir = Paths.get(baseDir).toAbsolutePath().normalize();
        this.stagingRoot = this.baseDir.resolve("_staging").normalize();
    }

    @Override
    public StoragePlan plan(String ownerUserId, TransactionId transactionId, AttachmentId attachmentId,
                            String uploadFileName, String contentType) {

        // contentType 기반 확장자 결정 (표시용 이름이 아니라 서버가 결정)
        String ext = extensionByContentType(contentType);
        String storedFileName = UUID.randomUUID().toString() + ext;

        // 최종 경로: 웹루트 밖에 저장하는 전제를 유지
        String storagePath = "u/" + ownerUserId + "/tx/" + transactionId + "/";

        // staging은 attachmentId 기준으로 저장 (확장자 없이)
        String stagingPath = "u/" + ownerUserId + "/tx/" + transactionId + "/";
        String stagingFileName = attachmentId + ".upload";

        return new StoragePlan(uploadFileName, contentType, 0L,
                storagePath, storedFileName,
                stagingPath, stagingFileName);
    }

    @Override
    public void saveToStaging(StoragePlan plan, MultipartFile file) {
        validateImage(file);

        // sizeBytes는 업로드 파일에서 확정되므로 plan을 다시 만들기보단, 서비스에서 별도 보관해도 됨
        Path dir = safeResolve(stagingRoot, plan.getStagingPath());
        Path target = dir.resolve(plan.getStagingFileName()).normalize();

        try {
            Files.createDirectories(dir);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new IllegalStateException("failed to save staging file", e);
        }
    }

    @Override
    public void moveStagingToFinal(StoragePlan plan) {
        Path stagingFile = safeResolve(stagingRoot, plan.getStagingPath())
                .resolve(plan.getStagingFileName()).normalize();

        Path finalDir = safeResolve(baseDir, plan.getStoragePath());
        Path finalFile = finalDir.resolve(plan.getStoredFileName()).normalize();

        try {
            Files.createDirectories(finalDir);

            // 동일 FS면 ATOMIC_MOVE 시도
            try {
                Files.move(stagingFile, finalFile,
                        StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(stagingFile, finalFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new IllegalStateException("failed to move staging -> final", e);
        }
    }

    @Override
    public void deleteStagingQuietly(StoragePlan plan) {
        try {
            Path p = safeResolve(stagingRoot, plan.getStagingPath())
                    .resolve(plan.getStagingFileName()).normalize();
            if (p.startsWith(stagingRoot)) {
                Files.deleteIfExists(p);
            }
        } catch (Exception ignored) {
        }
    }

    private Path safeResolve(Path base, String relative) {
        Path resolved = base.resolve(relative).normalize();
        if (!resolved.startsWith(base)) {
            throw new IllegalArgumentException("invalid path (possible traversal)");
        }
        return resolved;
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("file required");
        if (file.getSize() <= 0 || file.getSize() > 5L * 1024 * 1024) {
            throw new IllegalArgumentException("file too large");
        }
        String ct = file.getContentType();
        if (ct == null) throw new IllegalArgumentException("contentType required");
        if (!("image/jpeg".equals(ct) || "image/png".equals(ct) || "image/webp".equals(ct))) {
            throw new IllegalArgumentException("only jpeg/png/webp allowed");
        }
    }

    private String extensionByContentType(String contentType) {
        String ct = contentType.toLowerCase(Locale.ROOT);
        if ("image/jpeg".equals(ct)) return ".jpg";
        if ("image/png".equals(ct)) return ".png";
        if ("image/webp".equals(ct)) return ".webp";
        return "";
    }
}
