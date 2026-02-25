package com.payv.automation.domain.repository;

import com.payv.automation.domain.model.FixedExpenseDefinitionId;
import com.payv.automation.domain.model.FixedExpenseExecution;
import com.payv.automation.domain.model.FixedExpenseExecutionId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 고정비 실행 인스턴스 영속 포트.
 */
public interface FixedExpenseExecutionRepository {

    /**
     * 실행 인스턴스를 저장한다. 구현체에서는 upsert로 처리한다.
     */
    void save(FixedExpenseExecution execution);

    /**
     * 특정 사용자 소유의 실행 인스턴스 1건 조회.
     */
    Optional<FixedExpenseExecution> findById(FixedExpenseExecutionId executionId, String ownerUserId);

    /**
     * (owner, definition, scheduledDate) 조합의 기존 데이터 존재 여부 조회.
     * 배치 재실행 시 중복 생성 방지(idempotency)에 사용한다.
     */
    boolean existsByDefinitionAndScheduledDate(String ownerUserId,
                                               FixedExpenseDefinitionId definitionId,
                                               LocalDate scheduledDate);

    /**
     * 특정 날짜의 PLANNED 상태 실행 인스턴스 조회.
     */
    List<FixedExpenseExecution> findPlannedByDate(LocalDate scheduledDate);
}
