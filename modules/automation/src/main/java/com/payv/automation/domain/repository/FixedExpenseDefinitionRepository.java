package com.payv.automation.domain.repository;

import com.payv.automation.domain.model.FixedExpenseDefinition;
import com.payv.automation.domain.model.FixedExpenseDefinitionId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 고정비 마스터 영속 포트.
 *
 * 도메인/애플리케이션 계층은 이 인터페이스만 의존하고,
 * 실제 저장 구현(MyBatis/JPA 등)은 infrastructure에서 제공한다.
 */
public interface FixedExpenseDefinitionRepository {

    /**
     * 마스터를 저장한다. 구현체에서는 upsert로 처리한다.
     */
    void save(FixedExpenseDefinition definition);

    /**
     * 특정 사용자 소유의 마스터 1건 조회.
     */
    Optional<FixedExpenseDefinition> findById(FixedExpenseDefinitionId definitionId, String ownerUserId);

    /**
     * 사용자 기준 활성 마스터 조회.
     */
    List<FixedExpenseDefinition> findAllActiveByOwner(String ownerUserId);

    /**
     * runDate에 실행 대상인 활성 마스터 조회.
     */
    List<FixedExpenseDefinition> findAllActiveScheduledOn(LocalDate runDate);
}
