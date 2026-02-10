package com.payv.budget.application.command;

import com.payv.budget.application.command.model.CreateBudgetCommand;
import com.payv.budget.application.command.model.DeactivateBudgetCommand;
import com.payv.budget.application.command.model.UpdateBudgetCommand;
import com.payv.budget.application.port.ClassificationQueryPort;
import com.payv.budget.application.port.ClassificationValidationPort;
import com.payv.budget.domain.model.Budget;
import com.payv.budget.domain.model.BudgetId;
import com.payv.budget.domain.repository.BudgetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BudgetCommandService {

    private final BudgetRepository budgetRepository;

    private final ClassificationValidationPort classificationValidationPort;
    private final ClassificationQueryPort classificationQueryPort;

    public BudgetId create(CreateBudgetCommand command, String ownerUserId) {
        Objects.requireNonNull(command, "command");
        requireOwner(ownerUserId);
        cleanupOrphanedCategoryBudgets(ownerUserId);

        validateCategoryIfPresent(command.getCategoryId(), ownerUserId);
        ensureUniqueBudget(ownerUserId, command.getTargetMonth(), command.getCategoryId(), null);

        Budget budget = Budget.create(
                ownerUserId,
                command.getTargetMonth(),
                command.getAmountLimit(),
                command.getCategoryId(),
                command.getMemo()
        );

        budgetRepository.save(budget, ownerUserId);
        return budget.getId();
    }

    public void update(UpdateBudgetCommand command, String ownerUserId) {
        Objects.requireNonNull(command, "command");
        requireOwner(ownerUserId);
        cleanupOrphanedCategoryBudgets(ownerUserId);

        Budget budget = budgetRepository.findById(command.getBudgetId(), ownerUserId)
                .orElseThrow(() -> new IllegalStateException("budget not found"));

        validateCategoryIfPresent(command.getCategoryId(), ownerUserId);
        ensureUniqueBudget(ownerUserId, command.getTargetMonth(), command.getCategoryId(), budget.getId());

        budget.update(
                command.getTargetMonth(),
                command.getAmountLimit(),
                command.getCategoryId(),
                command.getMemo()
        );

        budgetRepository.save(budget, ownerUserId);
    }

    public void deactivate(DeactivateBudgetCommand command, String ownerUserId) {
        Objects.requireNonNull(command, "command");
        requireOwner(ownerUserId);
        cleanupOrphanedCategoryBudgets(ownerUserId);

        Budget budget = budgetRepository.findById(command.getBudgetId(), ownerUserId)
                .orElseThrow(() -> new IllegalStateException("budget not found"));

        budget.deactivate();
        budgetRepository.save(budget, ownerUserId);
    }

    // category 삭제/비활성화 전파 정책: orphan category budget soft-delete.
    public void cleanupOrphanedCategoryBudgets(String ownerUserId) {
        requireOwner(ownerUserId);

        Set<String> activeRootCategoryIds = classificationQueryPort.getAllCategories(ownerUserId).stream()
                .map(c -> c.getCategoryId())
                .collect(Collectors.toCollection(HashSet::new));
        budgetRepository.deactivateOrphanedCategoryBudgets(ownerUserId, activeRootCategoryIds);
    }

    private void validateCategoryIfPresent(String categoryId, String ownerUserId) {
        if (categoryId == null || categoryId.trim().isEmpty()) return;
        classificationValidationPort.validateCategorization(java.util.Collections.singleton(categoryId), ownerUserId);
    }

    private void ensureUniqueBudget(String ownerUserId, YearMonth targetMonth,
                                    String categoryId, BudgetId excludeId) {
        List<Budget> budgets = budgetRepository.findAllByOwnerAndMonth(ownerUserId, targetMonth);
        for (Budget budget : budgets) {
            if (excludeId != null && budget.getId().equals(excludeId)) {
                continue;
            }

            if (Objects.equals(budget.getCategoryId(), normalizeNullable(categoryId))) {
                throw new IllegalStateException("duplicate budget exists for month/category");
            }
        }
    }

    private String normalizeNullable(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static void requireOwner(String ownerUserId) {
        if (ownerUserId == null || ownerUserId.trim().isEmpty()) {
            throw new IllegalArgumentException("ownerUserId must not be blank");
        }
    }
}
