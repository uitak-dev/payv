package com.payv.automation.domain.model;

/**
 * 고정비 실행 인스턴스 처리 상태.
 *
 * - PLANNED: 실행 예정으로 생성만 된 상태
 * - SUCCEEDED: 실제 거래 생성까지 성공한 상태
 * - FAILED: 처리 중 오류 발생
 * - SKIPPED: 정책상 의도적으로 건너뜀
 */
public enum FixedExpenseExecutionStatus {
    PLANNED,
    SUCCEEDED,
    FAILED,
    SKIPPED
}
