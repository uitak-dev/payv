package com.payv.budget.domain.repository;

import com.payv.budget.domain.model.Budget;
import com.payv.budget.domain.model.BudgetId;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BudgetRepository {

    void save(Budget budget, String ownerUserId);

    Optional<Budget> findById(BudgetId budgetId, String ownerUserId);

    List<Budget> findAllByOwnerAndMonth(String ownerUserId, YearMonth targetMonth);

    int deactivateOrphanedCategoryBudgets(String ownerUserId, Set<String> activeRootCategoryIds);
}
