package com.payv.automation.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

/**
 * 고정비 실행 인스턴스 식별자 Value Object.
 */
@Getter
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class FixedExpenseExecutionId {

    private final String value;

    /**
     * 실행 인스턴스 신규 생성 시 UUID 기반 식별자를 발급한다.
     */
    public static FixedExpenseExecutionId generate() {
        return new FixedExpenseExecutionId(UUID.randomUUID().toString());
    }

    /**
     * DB/외부 문자열을 도메인 ID 타입으로 복원한다.
     */
    public static FixedExpenseExecutionId of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("fixedExpenseExecutionId must not be blank");
        }
        return new FixedExpenseExecutionId(value);
    }
}
