package com.payv.automation.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

/**
 * 고정비 마스터 식별자 Value Object.
 */
@Getter
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class FixedExpenseDefinitionId {

    private final String value;

    /**
     * 신규 마스터 생성 시 UUID 기반 식별자를 발급한다.
     */
    public static FixedExpenseDefinitionId generate() {
        return new FixedExpenseDefinitionId(UUID.randomUUID().toString());
    }

    /**
     * DB/외부 입력 문자열을 도메인 ID 타입으로 복원한다.
     */
    public static FixedExpenseDefinitionId of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("fixedExpenseDefinitionId must not be blank");
        }
        return new FixedExpenseDefinitionId(value);
    }
}
