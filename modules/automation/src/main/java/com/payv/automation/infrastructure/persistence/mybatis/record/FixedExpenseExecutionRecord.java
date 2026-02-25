package com.payv.automation.infrastructure.persistence.mybatis.record;

import com.payv.automation.domain.model.FixedExpenseDefinitionId;
import com.payv.automation.domain.model.FixedExpenseExecution;
import com.payv.automation.domain.model.FixedExpenseExecutionId;
import com.payv.automation.domain.model.FixedExpenseExecutionStatus;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * fixed_expense_execution 테이블 매핑용 Record DTO.
 */
@Data
@NoArgsConstructor
public class FixedExpenseExecutionRecord {

    private String executionId;
    private String definitionId;
    private String ownerUserId;

    private String definitionName;
    private long amount;
    private String assetId;
    private String categoryIdLevel1;
    private String categoryIdLevel2;
    private String memo;

    private LocalDate scheduledDate;
    private String status;
    private String transactionId;
    private String failureReason;
    private Long batchJobExecutionId;
    private LocalDateTime processedAt;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @Builder
    private FixedExpenseExecutionRecord(String executionId,
                                        String definitionId,
                                        String ownerUserId,
                                        String definitionName,
                                        long amount,
                                        String assetId,
                                        String categoryIdLevel1,
                                        String categoryIdLevel2,
                                        String memo,
                                        LocalDate scheduledDate,
                                        String status,
                                        String transactionId,
                                        String failureReason,
                                        Long batchJobExecutionId,
                                        LocalDateTime processedAt,
                                        OffsetDateTime createdAt,
                                        OffsetDateTime updatedAt) {
        this.executionId = executionId;
        this.definitionId = definitionId;
        this.ownerUserId = ownerUserId;
        this.definitionName = definitionName;
        this.amount = amount;
        this.assetId = assetId;
        this.categoryIdLevel1 = categoryIdLevel1;
        this.categoryIdLevel2 = categoryIdLevel2;
        this.memo = memo;
        this.scheduledDate = scheduledDate;
        this.status = status;
        this.transactionId = transactionId;
        this.failureReason = failureReason;
        this.batchJobExecutionId = batchJobExecutionId;
        this.processedAt = processedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static FixedExpenseExecutionRecord toRecord(FixedExpenseExecution execution) {
        // 도메인 객체 -> DB 저장 형태
        return FixedExpenseExecutionRecord.builder()
                .executionId(execution.getId().getValue())
                .definitionId(execution.getDefinitionId().getValue())
                .ownerUserId(execution.getOwnerUserId())
                .definitionName(execution.getDefinitionName())
                .amount(execution.getAmount())
                .assetId(execution.getAssetId())
                .categoryIdLevel1(execution.getCategoryIdLevel1())
                .categoryIdLevel2(execution.getCategoryIdLevel2())
                .memo(execution.getMemo())
                .scheduledDate(execution.getScheduledDate())
                .status(execution.getStatus().name())
                .transactionId(execution.getTransactionId())
                .failureReason(execution.getFailureReason())
                .batchJobExecutionId(execution.getBatchJobExecutionId())
                .processedAt(execution.getProcessedAt())
                .build();
    }

    public FixedExpenseExecution toEntity() {
        // DB row -> 도메인 객체 복원
        return FixedExpenseExecution.of(
                FixedExpenseExecutionId.of(executionId),
                FixedExpenseDefinitionId.of(definitionId),
                ownerUserId,
                definitionName,
                amount,
                assetId,
                categoryIdLevel1,
                categoryIdLevel2,
                memo,
                scheduledDate,
                FixedExpenseExecutionStatus.valueOf(status),
                transactionId,
                failureReason,
                batchJobExecutionId,
                processedAt
        );
    }
}
