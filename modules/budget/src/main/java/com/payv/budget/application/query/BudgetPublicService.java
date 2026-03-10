package com.payv.budget.application.query;

import com.payv.budget.application.query.model.BudgetView;
import com.payv.contracts.budget.BudgetPublicApi;
import com.payv.contracts.budget.dto.BudgetUsagePublicDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BudgetPublicService implements BudgetPublicApi {

    private final BudgetQueryService budgetQueryService;

    @Override
    public List<BudgetUsagePublicDto> getMonthlyBudgetUsages(String ownerUserId, YearMonth targetMonth) {
        if (targetMonth == null) {
            return Collections.emptyList();
        }

        List<BudgetView> views = budgetQueryService.getMonthlyBudgets(ownerUserId, targetMonth);
        if (views == null || views.isEmpty()) {
            return Collections.emptyList();
        }

        List<BudgetUsagePublicDto> result = new ArrayList<>(views.size());
        for (BudgetView view : views) {
            result.add(new BudgetUsagePublicDto(
                    view.getBudgetId(),
                    parseMonth(view.getTargetMonth(), targetMonth),
                    view.getCategoryId(),
                    view.getCategoryName(),
                    view.getAmountLimit(),
                    view.getSpentAmount(),
                    view.getRemainingAmount(),
                    view.getUsageRate()
            ));
        }
        return result;
    }

    @Override
    public Optional<BudgetUsagePublicDto> getOverallBudgetUsage(String ownerUserId, YearMonth targetMonth) {
        if (targetMonth == null) {
            return Optional.empty();
        }
        return getMonthlyBudgetUsages(ownerUserId, targetMonth).stream()
                .filter(row -> row.getCategoryId() == null)
                .findFirst();
    }

    private YearMonth parseMonth(String value, YearMonth fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        try {
            return YearMonth.parse(value.trim());
        } catch (Exception e) {
            return fallback;
        }
    }
}
