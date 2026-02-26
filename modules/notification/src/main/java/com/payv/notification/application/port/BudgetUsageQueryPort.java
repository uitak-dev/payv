package com.payv.notification.application.port;

import com.payv.notification.application.port.dto.BudgetUsageSnapshot;

import java.time.YearMonth;
import java.util.List;

public interface BudgetUsageQueryPort {

    List<BudgetUsageSnapshot> findMonthlyBudgetUsages(String ownerUserId, YearMonth targetMonth);
}
