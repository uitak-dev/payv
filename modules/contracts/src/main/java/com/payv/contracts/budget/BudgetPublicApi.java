package com.payv.contracts.budget;

import com.payv.contracts.budget.dto.BudgetUsagePublicDto;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface BudgetPublicApi {

    List<BudgetUsagePublicDto> getMonthlyBudgetUsages(String ownerUserId, YearMonth targetMonth);

    Optional<BudgetUsagePublicDto> getOverallBudgetUsage(String ownerUserId, YearMonth targetMonth);
}
