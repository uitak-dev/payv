package com.payv.automation.infrastructure.persistence.mybatis;

import com.payv.automation.domain.model.FixedExpenseDefinitionId;
import com.payv.automation.domain.model.FixedExpenseExecution;
import com.payv.automation.domain.model.FixedExpenseExecutionId;
import com.payv.automation.domain.repository.FixedExpenseExecutionRepository;
import com.payv.automation.infrastructure.persistence.mybatis.mapper.FixedExpenseExecutionMapper;
import com.payv.automation.infrastructure.persistence.mybatis.record.FixedExpenseExecutionRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * FixedExpenseExecutionRepository의 MyBatis 구현체.
 */
@Repository
@RequiredArgsConstructor
public class MyBatisFixedExpenseExecutionRepository implements FixedExpenseExecutionRepository {

    private final FixedExpenseExecutionMapper mapper;

    @Override
    public void save(FixedExpenseExecution execution) {
        // DB에서는 upsert로 신규/수정을 동일하게 처리
        mapper.upsert(FixedExpenseExecutionRecord.toRecord(execution));
    }

    @Override
    public Optional<FixedExpenseExecution> findById(FixedExpenseExecutionId executionId, String ownerUserId) {
        FixedExpenseExecutionRecord record = mapper.selectByIdAndOwner(executionId.getValue(), ownerUserId);
        if (record == null) {
            return Optional.empty();
        }
        return Optional.of(record.toEntity());
    }

    @Override
    public boolean existsByDefinitionAndScheduledDate(String ownerUserId,
                                                      FixedExpenseDefinitionId definitionId,
                                                      LocalDate scheduledDate) {
        // 배치 중복 생성 방지용 존재 체크
        return mapper.countByDefinitionAndScheduledDate(ownerUserId, definitionId.getValue(), scheduledDate) > 0;
    }

    @Override
    public List<FixedExpenseExecution> findPlannedByDate(LocalDate scheduledDate) {
        List<FixedExpenseExecutionRecord> rows = mapper.selectPlannedByDate(scheduledDate);
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        return rows.stream().map(FixedExpenseExecutionRecord::toEntity).collect(Collectors.toList());
    }
}
