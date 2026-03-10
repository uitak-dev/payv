package com.payv.reporting.infrastructure.adapter;

import com.payv.contracts.budget.BudgetPublicApi;
import com.payv.reporting.application.port.BudgetSnapshotPort;
import com.payv.reporting.application.port.dto.OverallBudgetSnapshotDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.Optional;

@Component("reportingBudgetAclAdapter")
@RequiredArgsConstructor
public class InProcessBudgetAclAdapter implements BudgetSnapshotPort {

    private final BudgetPublicApi budgetPublicService;

    @Override
    public Optional<OverallBudgetSnapshotDto> findOverallBudget(String ownerUserId, YearMonth targetMonth) {
        return budgetPublicService.getOverallBudgetUsage(ownerUserId, targetMonth)
                .map(overallBudget -> new OverallBudgetSnapshotDto(
                        overallBudget.getBudgetId(),
                        overallBudget.getAmountLimit(),
                        overallBudget.getSpentAmount(),
                        overallBudget.getRemainingAmount(),
                        overallBudget.getUsageRate()
                ));
    }
}
