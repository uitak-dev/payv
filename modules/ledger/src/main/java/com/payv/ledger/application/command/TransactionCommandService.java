package com.payv.ledger.application.command;

import com.payv.common.event.ledger.LedgerTransactionChangeType;
import com.payv.common.event.ledger.LedgerTransactionChangedEvent;
import com.payv.common.event.ledger.LedgerTransactionSnapshot;
import com.payv.common.cache.CacheNames;
import com.payv.ledger.application.command.model.CreateTransactionCommand;
import com.payv.ledger.application.command.model.CreateAutoFixedExpenseTransactionCommand;
import com.payv.ledger.application.command.model.UpdateTransactionCommand;
import com.payv.ledger.application.exception.TransactionNotFoundException;
import com.payv.ledger.application.port.AssetValidationPort;
import com.payv.ledger.application.port.AttachmentStoragePort;
import com.payv.ledger.application.port.ClassificationValidationPort;
import com.payv.ledger.application.port.TransactionChangedEventOutboxPort;
import com.payv.ledger.domain.model.Attachment;
import com.payv.ledger.domain.model.Transaction;
import com.payv.ledger.domain.model.TransactionId;
import com.payv.ledger.domain.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;

@Service
@RequiredArgsConstructor
/**
 * 거래(Transaction) 쓰기 시나리오를 담당하는 서비스.
 * - 수기 거래/자동 고정비 거래 생성, 거래 수정/삭제를 처리한다.
 * - 거래 변경 이벤트를 Outbox에 적재한다.
 * - 분류/자산 검증과 이벤트 발행(Outbox)을 동일 트랜잭션 경계에서 처리해
 *   데이터 정합성과 메시지 전달 신뢰성을 동시에 확보한다.
 */
public class TransactionCommandService {

    private final TransactionRepository transactionRepository;

    private final ClassificationValidationPort classificationValidationPort;
    private final AssetValidationPort assetValidationPort;

    private final AttachmentStoragePort attachmentStoragePort;

    private final TransactionChangedEventOutboxPort transactionChangedEventOutboxPort;

    /**
     * 수기 거래를 생성한다.
     *
     * Business logic:
     * - 태그/카테고리/자산 유효성 검증 후 도메인 엔티티를 생성한다.
     * - 저장 성공 시 변경 이벤트를 Outbox에 적재한다.
     *
     * @param command 생성 요청(거래 유형/금액/일자/분류/태그/메모)
     * @param ownerUserId 소유 사용자 ID
     * @return 생성된 거래 ID
     */
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.LEDGER_RECENT_FIRST_PAGE, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.BUDGET_MONTHLY_STATUS, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.REPORTING_MONTHLY_SUMMARY, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.REPORTING_HOME_DASHBOARD, allEntries = true)
    })
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
        assetValidationPort.validateAssetIds(Collections.singleton(command.getAssetId()), ownerUserId);

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

    /**
     * 자동 생성된 고정비 거래를 생성한다.
     *
     * @param command 고정비 자동 생성 요청(정의 ID, 금액, 자산, 카테고리, 거래일)
     * @param ownerUserId 소유 사용자 ID
     * @return 생성된 거래 ID
     */
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.LEDGER_RECENT_FIRST_PAGE, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.BUDGET_MONTHLY_STATUS, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.REPORTING_MONTHLY_SUMMARY, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.REPORTING_HOME_DASHBOARD, allEntries = true)
    })
    @Transactional
    public TransactionId createFixedCostAuto(CreateAutoFixedExpenseTransactionCommand command, String ownerUserId) {
        Objects.requireNonNull(command, "command");

        classificationValidationPort.validateCategorization(
                buildCategoryIds(command.getCategoryIdLevel1(), command.getCategoryIdLevel2()),
                ownerUserId
        );
        assetValidationPort.validateAssetIds(Collections.singleton(command.getAssetId()), ownerUserId);

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

    /**
     * 기존 거래를 수정한다.
     *
     * @param transactionId 수정 대상 거래 ID
     * @param command 수정 요청(거래 기본정보/분류/태그/메모)
     * @param ownerUserId 소유 사용자 ID
     * @throws TransactionNotFoundException 대상 거래를 찾지 못한 경우
     */
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.LEDGER_RECENT_FIRST_PAGE, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.BUDGET_MONTHLY_STATUS, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.REPORTING_MONTHLY_SUMMARY, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.REPORTING_HOME_DASHBOARD, allEntries = true)
    })
    @Transactional
    public void updateTransaction(TransactionId transactionId, UpdateTransactionCommand command, String ownerUserId) {
        if (command.getTagIds() != null && !command.getTagIds().isEmpty()) {
            classificationValidationPort.validateTagIds(command.getTagIds(), ownerUserId);
        }
        classificationValidationPort.validateCategorization(
                buildCategoryIds(command.getCategoryIdLevel1(), command.getCategoryIdLevel2()),
                ownerUserId
        );
        assetValidationPort.validateAssetIds(Collections.singleton(command.getAssetId()), ownerUserId);

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

    /**
     * 거래를 삭제한다.
     *
     * Business logic:
     * - 첨부파일 바이너리를 정리한 뒤 거래를 삭제한다.
     * - 삭제 전 스냅샷을 Outbox 이벤트로 남겨 후속 정책(알림/집계 갱신)에 사용한다.
     *
     * @param transactionId 삭제 대상 거래 ID
     * @param ownerUserId 소유 사용자 ID
     * @throws TransactionNotFoundException 대상 거래를 찾지 못한 경우
     */
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.LEDGER_RECENT_FIRST_PAGE, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.BUDGET_MONTHLY_STATUS, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.REPORTING_MONTHLY_SUMMARY, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.REPORTING_HOME_DASHBOARD, allEntries = true)
    })
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
        transactionChangedEventOutboxPort.enqueue(
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
