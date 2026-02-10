package com.payv.budget.infrastructure.persistence.mybatis;

import com.payv.budget.domain.model.Budget;
import com.payv.budget.domain.model.BudgetId;
import com.payv.budget.domain.repository.BudgetRepository;
import com.payv.budget.infrastructure.persistence.mybatis.mapper.BudgetMapper;
import com.payv.budget.infrastructure.persistence.mybatis.record.BudgetRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class MyBatisBudgetRepository implements BudgetRepository {

    private final BudgetMapper budgetMapper;

    @Override
    public void save(Budget budget, String ownerUserId) {
        budgetMapper.upsert(BudgetRecord.toRecord(budget));
    }

    @Override
    public Optional<Budget> findById(BudgetId budgetId, String ownerUserId) {
        BudgetRecord record = budgetMapper.selectByIdAndOwner(budgetId.getValue(), ownerUserId);
        if (record == null) return Optional.empty();
        return Optional.of(record.toEntity());
    }

    @Override
    public List<Budget> findAllByOwnerAndMonth(String ownerUserId, YearMonth targetMonth) {
        if (targetMonth == null) return Collections.emptyList();
        List<BudgetRecord> records = budgetMapper.selectAllByOwnerAndMonth(ownerUserId, targetMonth.toString());
        if (records == null || records.isEmpty()) return Collections.emptyList();
        return records.stream().map(BudgetRecord::toEntity).collect(Collectors.toList());
    }

    @Override
    public int deactivateOrphanedCategoryBudgets(String ownerUserId, Set<String> activeRootCategoryIds) {
        if (activeRootCategoryIds == null || activeRootCategoryIds.isEmpty()) {
            return budgetMapper.deactivateAllCategoryBudgets(ownerUserId);
        }
        return budgetMapper.deactivateCategoryBudgetsNotIn(
                ownerUserId,
                activeRootCategoryIds.stream().collect(Collectors.toList())
        );
    }
}
