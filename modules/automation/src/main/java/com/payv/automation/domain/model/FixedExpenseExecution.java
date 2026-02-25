package com.payv.automation.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 고정비 실제 실행(또는 실행 예정) 인스턴스.
 *
 * definition은 "규칙", execution은 "특정 날짜의 실제 처리 단위"다.
 * 실행 시점의 이름/금액/분류를 snapshot으로 보관해
 * 이후 definition이 바뀌어도 당시 이력을 재현할 수 있게 한다.
 */
@Getter
public final class FixedExpenseExecution {

    private final FixedExpenseExecutionId id;
    private final FixedExpenseDefinitionId definitionId;
    private final String ownerUserId;

    // 마스터 변경 이력과 독립적으로 유지하기 위해 스냅샷 보관
    private final String definitionName;
    private final long amount;
    private final String assetId;
    private final String categoryIdLevel1;
    private final String categoryIdLevel2;
    private final String memo;

    // 실거래 기준일(= ledger transactionDate)
    private final LocalDate scheduledDate;

    private FixedExpenseExecutionStatus status;
    private String transactionId;
    private String failureReason;
    private Long batchJobExecutionId;
    private LocalDateTime processedAt;

    @Builder
    private FixedExpenseExecution(FixedExpenseExecutionId id,
                                  FixedExpenseDefinitionId definitionId,
                                  String ownerUserId,
                                  String definitionName,
                                  long amount,
                                  String assetId,
                                  String categoryIdLevel1,
                                  String categoryIdLevel2,
                                  String memo,
                                  LocalDate scheduledDate,
                                  FixedExpenseExecutionStatus status,
                                  String transactionId,
                                  String failureReason,
                                  Long batchJobExecutionId,
                                  LocalDateTime processedAt) {
        this.id = requireId(id);
        this.definitionId = requireDefinitionId(definitionId);
        this.ownerUserId = requireText(ownerUserId, "ownerUserId");
        this.definitionName = requireText(definitionName, "definitionName");
        this.amount = requirePositive(amount, "amount");
        this.assetId = requireText(assetId, "assetId");
        this.categoryIdLevel1 = requireText(categoryIdLevel1, "categoryIdLevel1");
        this.categoryIdLevel2 = normalizeNullable(categoryIdLevel2);
        this.memo = normalizeNullable(memo);
        this.scheduledDate = requireDate(scheduledDate, "scheduledDate");
        this.status = status == null ? FixedExpenseExecutionStatus.PLANNED : status;
        this.transactionId = normalizeNullable(transactionId);
        this.failureReason = normalizeNullable(failureReason);
        this.batchJobExecutionId = batchJobExecutionId;
        this.processedAt = processedAt;
    }

    public static FixedExpenseExecution plan(FixedExpenseDefinition definition, LocalDate scheduledDate) {
        // 계획 생성 시점에는 상태를 PLANNED로 시작한다.
        return FixedExpenseExecution.builder()
                .id(FixedExpenseExecutionId.generate())
                .definitionId(definition.getId())
                .ownerUserId(definition.getOwnerUserId())
                .definitionName(definition.getName())
                .amount(definition.getAmount())
                .assetId(definition.getAssetId())
                .categoryIdLevel1(definition.getCategoryIdLevel1())
                .categoryIdLevel2(definition.getCategoryIdLevel2())
                .memo(definition.getMemo())
                .scheduledDate(scheduledDate)
                .status(FixedExpenseExecutionStatus.PLANNED)
                .build();
    }

    public static FixedExpenseExecution of(FixedExpenseExecutionId id,
                                           FixedExpenseDefinitionId definitionId,
                                           String ownerUserId,
                                           String definitionName,
                                           long amount,
                                           String assetId,
                                           String categoryIdLevel1,
                                           String categoryIdLevel2,
                                           String memo,
                                           LocalDate scheduledDate,
                                           FixedExpenseExecutionStatus status,
                                           String transactionId,
                                           String failureReason,
                                           Long batchJobExecutionId,
                                           LocalDateTime processedAt) {
        return FixedExpenseExecution.builder()
                .id(id)
                .definitionId(definitionId)
                .ownerUserId(ownerUserId)
                .definitionName(definitionName)
                .amount(amount)
                .assetId(assetId)
                .categoryIdLevel1(categoryIdLevel1)
                .categoryIdLevel2(categoryIdLevel2)
                .memo(memo)
                .scheduledDate(scheduledDate)
                .status(status)
                .transactionId(transactionId)
                .failureReason(failureReason)
                .batchJobExecutionId(batchJobExecutionId)
                .processedAt(processedAt)
                .build();
    }

    public void markSucceeded(String transactionId, Long batchJobExecutionId, LocalDateTime processedAt) {
        ensurePlanned();
        // 거래 생성까지 완료된 정상 종료 상태
        this.status = FixedExpenseExecutionStatus.SUCCEEDED;
        this.transactionId = requireText(transactionId, "transactionId");
        this.failureReason = null;
        this.batchJobExecutionId = batchJobExecutionId;
        this.processedAt = requireDateTime(processedAt, "processedAt");
    }

    public void markFailed(String failureReason, Long batchJobExecutionId, LocalDateTime processedAt) {
        ensurePlanned();
        // 재시도/장애 추적을 위해 실패 이유를 보관한다.
        this.status = FixedExpenseExecutionStatus.FAILED;
        this.failureReason = requireText(failureReason, "failureReason");
        this.batchJobExecutionId = batchJobExecutionId;
        this.processedAt = requireDateTime(processedAt, "processedAt");
    }

    public void markSkipped(String reason, Long batchJobExecutionId, LocalDateTime processedAt) {
        ensurePlanned();
        // 정책상 스킵한 경우(예: 중복 방지)도 처리 이력으로 남긴다.
        this.status = FixedExpenseExecutionStatus.SKIPPED;
        this.failureReason = requireText(reason, "reason");
        this.batchJobExecutionId = batchJobExecutionId;
        this.processedAt = requireDateTime(processedAt, "processedAt");
    }

    private void ensurePlanned() {
        // 한 번 완료된 실행 건을 다시 완료 처리하지 못하도록 상태 전이를 제한한다.
        if (this.status != FixedExpenseExecutionStatus.PLANNED) {
            throw new IllegalStateException("fixed expense execution is already finalized");
        }
    }

    private static FixedExpenseExecutionId requireId(FixedExpenseExecutionId value) {
        if (value == null) {
            throw new IllegalArgumentException("fixedExpenseExecutionId must not be null");
        }
        return value;
    }

    private static FixedExpenseDefinitionId requireDefinitionId(FixedExpenseDefinitionId value) {
        if (value == null) {
            throw new IllegalArgumentException("fixedExpenseDefinitionId must not be null");
        }
        return value;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    private static long requirePositive(long value, String fieldName) {
        if (value <= 0L) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
        return value;
    }

    private static LocalDate requireDate(LocalDate value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
        return value;
    }

    private static LocalDateTime requireDateTime(LocalDateTime value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
        return value;
    }

    private static String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
