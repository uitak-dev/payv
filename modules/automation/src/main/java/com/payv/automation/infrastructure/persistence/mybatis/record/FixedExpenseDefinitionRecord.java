package com.payv.automation.infrastructure.persistence.mybatis.record;

import com.payv.automation.domain.model.FixedExpenseCycle;
import com.payv.automation.domain.model.FixedExpenseDefinition;
import com.payv.automation.domain.model.FixedExpenseDefinitionId;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * fixed_expense_definition 테이블 매핑용 Record DTO.
 *
 * 도메인 엔티티와 DB 스키마를 직접 결합하지 않기 위해
 * 변환 메서드(toRecord/toEntity)로 경계를 분리한다.
 */
@Data
@NoArgsConstructor
public class FixedExpenseDefinitionRecord {

    private String definitionId;
    private String ownerUserId;
    private String name;
    private long amount;
    private String assetId;
    private String categoryIdLevel1;
    private String categoryIdLevel2;
    private String memo;

    private String cycle;
    private Integer dayOfMonth;
    private Boolean endOfMonth;
    private Boolean active;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @Builder
    private FixedExpenseDefinitionRecord(String definitionId,
                                         String ownerUserId,
                                         String name,
                                         long amount,
                                         String assetId,
                                         String categoryIdLevel1,
                                         String categoryIdLevel2,
                                         String memo,
                                         String cycle,
                                         Integer dayOfMonth,
                                         Boolean endOfMonth,
                                         Boolean active,
                                         OffsetDateTime createdAt,
                                         OffsetDateTime updatedAt) {
        this.definitionId = definitionId;
        this.ownerUserId = ownerUserId;
        this.name = name;
        this.amount = amount;
        this.assetId = assetId;
        this.categoryIdLevel1 = categoryIdLevel1;
        this.categoryIdLevel2 = categoryIdLevel2;
        this.memo = memo;
        this.cycle = cycle;
        this.dayOfMonth = dayOfMonth;
        this.endOfMonth = endOfMonth;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static FixedExpenseDefinitionRecord toRecord(FixedExpenseDefinition definition) {
        // 도메인 객체 -> DB 저장 형태
        return FixedExpenseDefinitionRecord.builder()
                .definitionId(definition.getId().getValue())
                .ownerUserId(definition.getOwnerUserId())
                .name(definition.getName())
                .amount(definition.getAmount())
                .assetId(definition.getAssetId())
                .categoryIdLevel1(definition.getCategoryIdLevel1())
                .categoryIdLevel2(definition.getCategoryIdLevel2())
                .memo(definition.getMemo())
                .cycle(definition.getCycle().name())
                .dayOfMonth(definition.getDayOfMonth())
                .endOfMonth(definition.isEndOfMonth())
                .active(definition.isActive())
                .build();
    }

    public FixedExpenseDefinition toEntity() {
        // DB row -> 도메인 객체 복원
        return FixedExpenseDefinition.of(
                FixedExpenseDefinitionId.of(definitionId),
                ownerUserId,
                name,
                amount,
                assetId,
                categoryIdLevel1,
                categoryIdLevel2,
                memo,
                FixedExpenseCycle.valueOf(cycle),
                dayOfMonth,
                Boolean.TRUE.equals(endOfMonth),
                Boolean.TRUE.equals(active)
        );
    }
}
