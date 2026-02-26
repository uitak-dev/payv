package com.payv.ledger.application.command;

import com.payv.common.event.ledger.LedgerTransactionChangeType;
import com.payv.common.event.ledger.LedgerTransactionChangedEvent;
import com.payv.common.event.ledger.LedgerTransactionSnapshot;
import com.payv.ledger.application.command.model.CreateTransactionCommand;
import com.payv.ledger.application.command.model.CreateAutoFixedExpenseTransactionCommand;
import com.payv.ledger.application.command.model.UpdateTransactionCommand;
import com.payv.ledger.application.exception.TransactionNotFoundException;
import com.payv.ledger.application.port.AssetValidationPort;
import com.payv.ledger.application.port.AttachmentStoragePort;
import com.payv.ledger.application.port.ClassificationValidationPort;
import com.payv.ledger.domain.model.Attachment;
import com.payv.ledger.domain.model.Transaction;
import com.payv.ledger.domain.model.TransactionId;
import com.payv.ledger.domain.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TransactionCommandService {

    private final TransactionRepository transactionRepository;

    private final ClassificationValidationPort classificationValidationPort;
    private final AssetValidationPort assetValidationPort;
    private final AttachmentStoragePort attachmentStoragePort;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public TransactionId createManual(CreateTransactionCommand command, String ownerUserId) {

        // 1) 유효성 검증.
        if (command.getTagIds() != null && !command.getTagIds().isEmpty()) {
            classificationValidationPort.validateTagIds(command.getTagIds(), ownerUserId);
        }
        classificationValidationPort.validateCategorization(
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
        publishTransactionChangedEvent(
                ownerUserId,
                LedgerTransactionChangeType.CREATED,
                null,
                toSnapshot(transaction)
        );
        return transaction.getId();
    }

    @Transactional
    public TransactionId createFixedCostAuto(CreateAutoFixedExpenseTransactionCommand command, String ownerUserId) {
        Objects.requireNonNull(command, "command");

        classificationValidationPort.validateCategorization(
                buildCategoryIds(command.getCategoryIdLevel1(), command.getCategoryIdLevel2()),
                ownerUserId
        );
        assetValidationPort.validateAssertId(command.getAssetId(), ownerUserId);

        Transaction transaction = Transaction.createFixedCostAuto(
                ownerUserId,
                command.getFixedExpenseDefinitionId(),
                com.payv.ledger.domain.model.TransactionType.EXPENSE,
                command.getAmount(),
                command.getTransactionDate(),
                command.getAssetId(),
                command.getCategoryIdLevel1()
        );
        transaction.updateMemo(command.getMemo());
        transaction.updateCategorize(command.getCategoryIdLevel1(), command.getCategoryIdLevel2());

        transactionRepository.save(transaction);
        publishTransactionChangedEvent(
                ownerUserId,
                LedgerTransactionChangeType.CREATED,
                null,
                toSnapshot(transaction)
        );
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
                .orElseThrow(TransactionNotFoundException::new);
        LedgerTransactionSnapshot beforeSnapshot = toSnapshot(tx);

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
        publishTransactionChangedEvent(
                ownerUserId,
                LedgerTransactionChangeType.UPDATED,
                beforeSnapshot,
                toSnapshot(tx)
        );
    }

    @Transactional
    public void deleteTransaction(TransactionId transactionId, String ownerUserId) {
        Transaction tx = transactionRepository.findById(transactionId, ownerUserId)
                .orElseThrow(TransactionNotFoundException::new);
        LedgerTransactionSnapshot beforeSnapshot = toSnapshot(tx);

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
        publishTransactionChangedEvent(
                ownerUserId,
                LedgerTransactionChangeType.DELETED,
                beforeSnapshot,
                null
        );
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

    private void publishTransactionChangedEvent(String ownerUserId,
                                                LedgerTransactionChangeType changeType,
                                                LedgerTransactionSnapshot before,
                                                LedgerTransactionSnapshot after) {
        eventPublisher.publishEvent(
                LedgerTransactionChangedEvent.builder()
                        .ownerUserId(ownerUserId)
                        .changeType(changeType)
                        .before(before)
                        .after(after)
                        .occurredAt(OffsetDateTime.now())
                        .build()
        );
    }

    private LedgerTransactionSnapshot toSnapshot(Transaction tx) {
        if (tx == null) {
            return null;
        }
        String sourceType = tx.getTransactionSource() == null || tx.getTransactionSource().getType() == null
                ? null : tx.getTransactionSource().getType().name();
        String sourceReference = tx.getTransactionSource() == null
                ? null : tx.getTransactionSource().getOriginalReference();

        return LedgerTransactionSnapshot.builder()
                .transactionId(tx.getId().getValue())
                .ownerUserId(tx.getOwnerUserId())
                .transactionType(tx.getTransactionType().name())
                .transactionDate(tx.getTransactionDate())
                .amount(tx.getAmount() == null || tx.getAmount().getAmount() == null ? 0L : tx.getAmount().getAmount())
                .sourceType(sourceType)
                .sourceReference(sourceReference)
                .build();
    }
}
