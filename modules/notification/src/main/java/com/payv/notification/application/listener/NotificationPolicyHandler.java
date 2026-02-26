package com.payv.notification.application.listener;

import com.payv.common.event.ledger.LedgerTransactionChangedEvent;
import com.payv.common.event.ledger.LedgerTransactionSnapshot;
import com.payv.notification.application.command.NotificationCommandService;
import com.payv.notification.application.port.BudgetUsageQueryPort;
import com.payv.notification.application.port.dto.BudgetUsageSnapshot;
import com.payv.notification.domain.model.NotificationDispatchLog;
import com.payv.notification.domain.model.NotificationType;
import com.payv.notification.domain.repository.NotificationDispatchLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.YearMonth;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class NotificationPolicyHandler {

    private static final String REF_TYPE_BUDGET = "BUDGET";
    private static final String REF_TYPE_TRANSACTION = "TRANSACTION";

    private final BudgetUsageQueryPort budgetUsageQueryPort;
    private final NotificationDispatchLogRepository dispatchLogRepository;
    private final NotificationCommandService notificationCommandService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLedgerTransactionChanged(LedgerTransactionChangedEvent event) {
        if (event == null || event.getOwnerUserId() == null || event.getOwnerUserId().trim().isEmpty()) {
            return;
        }

        notifyBudgetThresholds(event);
        notifyFixedExpenseAutoCreated(event);
    }

    private void notifyBudgetThresholds(LedgerTransactionChangedEvent event) {
        Set<YearMonth> affectedMonths = event.affectedExpenseMonths();
        if (affectedMonths == null || affectedMonths.isEmpty()) {
            return;
        }

        String ownerUserId = event.getOwnerUserId();
        for (YearMonth month : affectedMonths) {
            List<BudgetUsageSnapshot> snapshots = budgetUsageQueryPort.findMonthlyBudgetUsages(ownerUserId, month);
            for (BudgetUsageSnapshot snapshot : snapshots) {
                notifyBudgetThreshold(ownerUserId, snapshot, 50, NotificationType.BUDGET_THRESHOLD_50);
                notifyBudgetThreshold(ownerUserId, snapshot, 100, NotificationType.BUDGET_THRESHOLD_100);
            }
        }
    }

    private void notifyBudgetThreshold(String ownerUserId,
                                       BudgetUsageSnapshot snapshot,
                                       int threshold,
                                       NotificationType notificationType) {
        if (snapshot == null || snapshot.getUsageRate() < threshold) {
            return;
        }

        String dispatchKey = String.format(
                "%s:%s:%s",
                notificationType.name(),
                snapshot.getBudgetId(),
                snapshot.getTargetMonth()
        );

        boolean firstDispatch = dispatchLogRepository.appendIfAbsent(
                NotificationDispatchLog.of(dispatchKey, ownerUserId, notificationType)
        );
        if (!firstDispatch) {
            return;
        }

        String budgetName = safeBudgetName(snapshot.getCategoryName());
        String title = String.format("%s 예산 사용률 %d%% 도달", budgetName, threshold);
        String message = String.format(
                "%s 사용률이 %d%%를 넘었습니다. (%d%%, %s)",
                budgetName,
                threshold,
                snapshot.getUsageRate(),
                snapshot.getTargetMonth()
        );

        notificationCommandService.createInApp(
                ownerUserId,
                notificationType,
                title,
                message,
                REF_TYPE_BUDGET,
                snapshot.getBudgetId()
        );
    }

    private void notifyFixedExpenseAutoCreated(LedgerTransactionChangedEvent event) {
        if (!event.isFixedExpenseAutoCreated()) {
            return;
        }

        LedgerTransactionSnapshot snapshot = event.getAfter();
        if (snapshot == null) {
            return;
        }

        String ownerUserId = event.getOwnerUserId();
        String dispatchKey = "FIXED_AUTO_CREATED:" + snapshot.getTransactionId();

        boolean firstDispatch = dispatchLogRepository.appendIfAbsent(
                NotificationDispatchLog.of(dispatchKey, ownerUserId, NotificationType.FIXED_EXPENSE_AUTO_CREATED)
        );
        if (!firstDispatch) {
            return;
        }

        String title = "고정비 거래가 자동 생성되었습니다";
        String message = String.format(
                "자동 생성 거래가 등록되었습니다. (거래일: %s, 금액: %,d)",
                snapshot.getTransactionDate(),
                snapshot.getAmount()
        );

        notificationCommandService.createInApp(
                ownerUserId,
                NotificationType.FIXED_EXPENSE_AUTO_CREATED,
                title,
                message,
                REF_TYPE_TRANSACTION,
                snapshot.getTransactionId()
        );
    }

    private String safeBudgetName(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return "예산";
        }
        return categoryName.trim();
    }
}
