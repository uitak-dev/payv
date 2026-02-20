package com.payv.reporting.infrastructure.adapter;

import com.payv.budget.application.query.BudgetQueryService;
import com.payv.reporting.application.port.BudgetSnapshotPort;
import com.payv.reporting.application.port.dto.OverallBudgetSnapshotDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.Optional;

@Component("reportingBudgetAclAdapter")
@RequiredArgsConstructor
public class InProcessBudgetAclAdapter implements BudgetSnapshotPort {

    private final BudgetQueryService budgetQueryService;

    @Override
    public Optional<OverallBudgetSnapshotDto> findOverallBudget(String ownerUserId, YearMonth targetMonth) {
        return budgetQueryService.getMonthlyBudgets(ownerUserId, targetMonth).stream()
                .filter(budget -> budget.getCategoryId() == null)
                .findFirst()
                .map(budget -> new OverallBudgetSnapshotDto(
                        budget.getBudgetId(),
                        budget.getAmountLimit(),
                        budget.getSpentAmount(),
                        budget.getRemainingAmount(),
                        budget.getUsageRate()
                ));
    }
}
