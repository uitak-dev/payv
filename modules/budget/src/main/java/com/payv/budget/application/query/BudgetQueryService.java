package com.payv.budget.application.query;

import com.payv.budget.application.port.ClassificationQueryPort;
import com.payv.budget.application.port.LedgerSpendingQueryPort;
import com.payv.budget.application.query.model.BudgetView;
import com.payv.budget.domain.model.Budget;
import com.payv.budget.domain.model.BudgetId;
import com.payv.budget.domain.repository.BudgetRepository;
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

    public Optional<BudgetView> get(BudgetId budgetId, String ownerUserId) {
        return budgetRepository.findById(budgetId, ownerUserId)
                .map(budget -> {
                    YearMonth targetMonth = budget.getTargetMonth();
                    LocalDate monthStart = targetMonth.atDay(1);
                    LocalDate monthEnd = targetMonth.atEndOfMonth();

                    String categoryId = budget.getCategoryId();
                    Map<String, String> categoryNames = categoryId == null
                            ? Collections.emptyMap()
                            : classificationQueryPort.getCategoryNames(Collections.singleton(categoryId), ownerUserId);

                    long spent = ledgerSpendingQueryPort.sumExpenseAmount(
                            ownerUserId,
                            monthStart,
                            monthEnd,
                            categoryId
                    );
                    long remaining = budget.getAmountLimit() - spent;
                    int usageRate = (int) ((spent * 100.0d) / budget.getAmountLimit());

                    return new BudgetView(
                            budget.getId().getValue(),
                            targetMonth.toString(),
                            categoryId,
                            budget.isOverallBudget() ? "전체 예산" : categoryNames.get(categoryId),
                            budget.getAmountLimit(),
                            budget.getMemo(),
                            spent,
                            remaining,
                            usageRate
                    );
                });
    }
}
