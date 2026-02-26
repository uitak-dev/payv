package com.payv.notification.infrastructure.adapter;

import com.payv.budget.application.query.BudgetQueryService;
import com.payv.budget.application.query.model.BudgetView;
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

    private final BudgetQueryService budgetQueryService;

    @Override
    public List<BudgetUsageSnapshot> findMonthlyBudgetUsages(String ownerUserId, YearMonth targetMonth) {
        if (targetMonth == null) {
            return Collections.emptyList();
        }

        List<BudgetView> views = budgetQueryService.getMonthlyBudgets(ownerUserId, targetMonth);
        if (views == null || views.isEmpty()) {
            return Collections.emptyList();
        }

        List<BudgetUsageSnapshot> snapshots = new ArrayList<>(views.size());
        for (BudgetView view : views) {
            snapshots.add(new BudgetUsageSnapshot(
                    view.getBudgetId(),
                    targetMonth,
                    view.getCategoryId(),
                    view.getCategoryName(),
                    view.getAmountLimit(),
                    view.getSpentAmount(),
                    view.getUsageRate()
            ));
        }
        return snapshots;
    }
}
