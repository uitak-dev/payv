package com.payv.budget.application.query;

import com.payv.budget.application.port.ClassificationQueryPort;
import com.payv.budget.application.port.LedgerSpendingQueryPort;
import com.payv.budget.domain.model.Budget;
import com.payv.budget.domain.model.BudgetId;
import com.payv.budget.domain.repository.BudgetRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BudgetQueryService {

    private final BudgetRepository budgetRepository;

    private final ClassificationQueryPort classificationQueryPort;
    private final LedgerSpendingQueryPort ledgerSpendingQueryPort;

    public List<BudgetView> getMonthlyBudgets(String ownerUserId, YearMonth yearMonth) {
        YearMonth targetMonth = yearMonth == null ? YearMonth.now() : yearMonth;
        LocalDate monthStart = targetMonth.atDay(1);
        LocalDate monthEnd = targetMonth.atEndOfMonth();

        List<Budget> budgets = budgetRepository.findAllByOwnerAndMonth(ownerUserId, targetMonth).stream()
                .filter(Budget::isActive)
                .collect(Collectors.toList());

        Set<String> categoryIds = budgets.stream()
                .map(Budget::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<String, String> categoryNames = categoryIds.isEmpty()
                ? Collections.emptyMap()
                : classificationQueryPort.getCategoryNames(categoryIds, ownerUserId);

        List<BudgetView> ret = new ArrayList<>(budgets.size());
        for (Budget budget : budgets) {
            long spent = ledgerSpendingQueryPort.sumExpenseAmount(
                    ownerUserId,
                    monthStart,
                    monthEnd,
                    budget.getCategoryId()
            );

            long remaining = budget.getAmountLimit() - spent;
            int usageRate = (int) ((spent * 100.0d) / budget.getAmountLimit());

            ret.add(new BudgetView(
                    budget.getId().getValue(),
                    budget.getTargetMonth().toString(),
                    budget.getCategoryId(),
                    budget.isOverallBudget() ? "전체 예산" : categoryNames.get(budget.getCategoryId()),
                    budget.getAmountLimit(),
                    budget.getMemo(),
                    spent,
                    remaining,
                    usageRate
            ));
        }

        ret.sort(Comparator.comparing(BudgetView::getCategoryName, Comparator.nullsLast(String::compareTo)));
        return ret;
    }

    public Optional<BudgetView> get(BudgetId budgetId, String ownerUserId, YearMonth yearMonth) {
        return budgetRepository.findById(budgetId, ownerUserId)
                .map(b -> {
                    List<BudgetView> one = getMonthlyBudgets(ownerUserId, yearMonth).stream()
                            .filter(v -> v.getBudgetId().equals(b.getId().getValue()))
                            .collect(Collectors.toList());
                    return one.isEmpty() ? null : one.get(0);
                });
    }

    @Getter
    @AllArgsConstructor
    public static class BudgetView {
        private final String budgetId;
        private final String targetMonth;
        private final String categoryId;
        private final String categoryName;
        private final long amountLimit;
        private final String memo;
        private final long spentAmount;
        private final long remainingAmount;
        private final int usageRate;
    }
}
