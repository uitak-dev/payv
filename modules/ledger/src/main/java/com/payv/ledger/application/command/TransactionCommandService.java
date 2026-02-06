package com.payv.ledger.application.command;

import com.payv.ledger.application.command.model.CreateTransactionCommand;
import com.payv.ledger.application.command.model.UpdateTransactionCommand;
import com.payv.ledger.application.port.AssetValidationPort;
import com.payv.ledger.application.port.AttachmentStoragePort;
import com.payv.ledger.application.port.ClassificationValidationPort;
import com.payv.ledger.domain.model.Attachment;
import com.payv.ledger.domain.model.Transaction;
import com.payv.ledger.domain.model.TransactionId;
import com.payv.ledger.domain.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.LinkedHashSet;

@Service
@RequiredArgsConstructor
public class TransactionCommandService {

    private final TransactionRepository transactionRepository;

    private final ClassificationValidationPort classificationValidationPort;
    private final AssetValidationPort assetValidationPort;
    private final AttachmentStoragePort attachmentStoragePort;

    @Transactional
    public TransactionId createManual(CreateTransactionCommand command, String ownerUserId) {

        // 1) 유효성 검증.
        if (command.getTagIds() != null && !command.getTagIds().isEmpty()) {
            classificationValidationPort.validateTagIds(command.getTagIds(), ownerUserId);
        }
        classificationValidationPort.validateCategoryIds(
                buildCategoryIds(command.getCategoryIdLevel1(), command.getCategoryIdLevel2()),
                ownerUserId
        );
        assetValidationPort.validateAssertId(command.getAssetId(), ownerUserId);

        // 2) 도메인 생성( Mandatory Fields )
        Transaction transaction = Transaction.createManual(
                ownerUserId,
                command.getTransactionType(),
                command.getAmount(),
                command.getTransactionDate(),
                command.getAssetId(),
                command.getCategoryIdLevel1()
        );

        // 3) 상세 추가( Optional Fields )
        transaction.updateMemo(command.getMemo());
        transaction.updateCategorize(transaction.getCategoryIdLevel1(), command.getCategoryIdLevel2());
        transaction.updateTags(command.getTagIds());

        // 4) 저장.
        transactionRepository.save(transaction);
        return transaction.getId();
    }

    @Transactional
    public void updateTransaction(TransactionId transactionId, UpdateTransactionCommand command, String ownerUserId) {
        if (command.getTagIds() != null && !command.getTagIds().isEmpty()) {
            classificationValidationPort.validateTagIds(command.getTagIds(), ownerUserId);
        }
        classificationValidationPort.validateCategorization(
                buildCategoryIds(command.getCategoryIdLevel1(), command.getCategoryIdLevel2()),
                ownerUserId
        );
        assetValidationPort.validateAssertId(command.getAssetId(), ownerUserId);

        Transaction tx = transactionRepository.findById(transactionId, ownerUserId)
                .orElseThrow(() -> new IllegalStateException("transaction not found"));

        tx.updateBasics(
                command.getTransactionType(),
                command.getAmount(),
                command.getTransactionDate(),
                command.getAssetId()
        );
        tx.updateMemo(command.getMemo());
        tx.updateCategorize(command.getCategoryIdLevel1(), command.getCategoryIdLevel2());
        tx.updateTags(command.getTagIds());

        transactionRepository.save(tx);
    }

    @Transactional
    public void deleteTransaction(TransactionId transactionId, String ownerUserId) {
        Transaction tx = transactionRepository.findById(transactionId, ownerUserId)
                .orElseThrow(() -> new IllegalStateException("transaction not found"));

        // 첨부 파일(메타) 삭제 전에 첨부 파일(바이너리) 정리.
        for (Attachment attachment : tx.getAttachments()) {
            AttachmentStoragePort.StoragePlan plan = new AttachmentStoragePort.StoragePlan(
                    attachment.getUploadFileName(),
                    attachment.getContentType(),
                    attachment.getSizeBytes(),
                    attachment.getStoragePath(),
                    attachment.getStoredFileName(),
                    attachment.getStagingPath(),
                    attachment.getStagingFileName()
            );
            attachmentStoragePort.deleteFinalQuietly(plan);
            attachmentStoragePort.deleteStagingQuietly(plan);
        }

        transactionRepository.deleteById(transactionId, ownerUserId);
    }

    private static Collection<String> buildCategoryIds(String categoryIdLevel1, String categoryIdLevel2) {
        LinkedHashSet<String> ids = new LinkedHashSet<>();
        if (categoryIdLevel1 != null && !categoryIdLevel1.trim().isEmpty()) {
            ids.add(categoryIdLevel1);
        }
        if (categoryIdLevel2 != null && !categoryIdLevel2.trim().isEmpty()) {
            ids.add(categoryIdLevel2);
        }
        return ids;
    }
}
