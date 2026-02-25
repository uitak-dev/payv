package com.payv.automation.infrastructure.persistence.mybatis;

import com.payv.automation.domain.model.FixedExpenseDefinition;
import com.payv.automation.domain.model.FixedExpenseDefinitionId;
import com.payv.automation.domain.repository.FixedExpenseDefinitionRepository;
import com.payv.automation.infrastructure.persistence.mybatis.mapper.FixedExpenseDefinitionMapper;
import com.payv.automation.infrastructure.persistence.mybatis.record.FixedExpenseDefinitionRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * FixedExpenseDefinitionRepository의 MyBatis 구현체.
 */
@Repository
@RequiredArgsConstructor
public class MyBatisFixedExpenseDefinitionRepository implements FixedExpenseDefinitionRepository {

    private final FixedExpenseDefinitionMapper mapper;

    @Override
    public void save(FixedExpenseDefinition definition) {
        // DB에서는 upsert로 신규/수정을 동일하게 처리
        mapper.upsert(FixedExpenseDefinitionRecord.toRecord(definition));
    }

    @Override
    public Optional<FixedExpenseDefinition> findById(FixedExpenseDefinitionId definitionId, String ownerUserId) {
        FixedExpenseDefinitionRecord record = mapper.selectByIdAndOwner(definitionId.getValue(), ownerUserId);
        if (record == null) {
            return Optional.empty();
        }
        return Optional.of(record.toEntity());
    }

    @Override
    public List<FixedExpenseDefinition> findAllActiveByOwner(String ownerUserId) {
        List<FixedExpenseDefinitionRecord> rows = mapper.selectAllActiveByOwner(ownerUserId);
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        return rows.stream().map(FixedExpenseDefinitionRecord::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<FixedExpenseDefinition> findAllActiveScheduledOn(LocalDate runDate) {
        if (runDate == null) {
            return Collections.emptyList();
        }
        int runDay = runDate.getDayOfMonth();
        int lastDayOfMonth = runDate.lengthOfMonth();
        boolean runDateIsEom = runDay == lastDayOfMonth;

        List<FixedExpenseDefinitionRecord> rows = mapper.selectAllActiveScheduledOn(runDay, lastDayOfMonth, runDateIsEom);
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        return rows.stream().map(FixedExpenseDefinitionRecord::toEntity).collect(Collectors.toList());
    }
}
