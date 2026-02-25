package com.payv.automation.domain.model;

/**
 * 고정비 반복 주기.
 *
 * 현재 정책상 "월 단위"만 허용하므로 MONTHLY 하나만 유지한다.
 * 향후 주/분기/연 단위를 지원할 때 enum 상수를 확장하면 된다.
 */
public enum FixedExpenseCycle {
    MONTHLY
}
