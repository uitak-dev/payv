package com.payv.notification.infrastructure.adapter;

import com.payv.contracts.budget.BudgetPublicApi;
import com.payv.contracts.budget.dto.BudgetUsagePublicDto;
import com.payv.notification.application.port.BudgetUsageQueryPort;
import com.payv.notification.application.port.dto.BudgetUsageSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component("notificationBudgetAclAdapter")
@RequiredArgsConstructor
public class InProcessBudgetAclAdapter implements BudgetUsageQueryPort {

    private final BudgetPublicApi budgetPublicService;

    @Override
    public List<BudgetUsageSnapshot> findMonthlyBudgetUsages(String ownerUserId, YearMonth targetMonth) {
        if (targetMonth == null) {
            return Collections.emptyList();
        }

        List<BudgetUsagePublicDto> rows = budgetPublicService.getMonthlyBudgetUsages(ownerUserId, targetMonth);
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }

        List<BudgetUsageSnapshot> snapshots = new ArrayList<>(rows.size());
        for (BudgetUsagePublicDto row : rows) {
            snapshots.add(new BudgetUsageSnapshot(
                    row.getBudgetId(),
                    row.getTargetMonth(),
                    row.getCategoryId(),
                    row.getCategoryName(),
                    row.getAmountLimit(),
                    row.getSpentAmount(),
                    row.getUsageRate()
            ));
        }
        return snapshots;
    }
}
