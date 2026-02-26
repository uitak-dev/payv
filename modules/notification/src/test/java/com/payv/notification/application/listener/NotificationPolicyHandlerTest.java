package com.payv.notification.application.listener;

import com.payv.common.event.ledger.LedgerTransactionChangeType;
import com.payv.common.event.ledger.LedgerTransactionChangedEvent;
import com.payv.common.event.ledger.LedgerTransactionSnapshot;
import com.payv.notification.application.command.NotificationCommandService;
import com.payv.notification.application.port.BudgetUsageQueryPort;
import com.payv.notification.application.port.dto.BudgetUsageSnapshot;
import com.payv.notification.domain.repository.NotificationDispatchLogRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.Assert.assertEquals;

public class NotificationPolicyHandlerTest {

    private BudgetUsageQueryPort budgetUsageQueryPort;
    private NotificationDispatchLogRepository dispatchLogRepository;
    private NotificationCommandService notificationCommandService;

    private NotificationPolicyHandler handler;

    @Before
    public void setUp() {
        budgetUsageQueryPort = mock(BudgetUsageQueryPort.class);
        dispatchLogRepository = mock(NotificationDispatchLogRepository.class);
        notificationCommandService = mock(NotificationCommandService.class);

        handler = new NotificationPolicyHandler(
                budgetUsageQueryPort,
                dispatchLogRepository,
                notificationCommandService
        );
    }

    @Test
    public void shouldPublishBudget50And100NotificationsWhenUsageExceeds100() {
        LedgerTransactionChangedEvent event = LedgerTransactionChangedEvent.builder()
                .ownerUserId("usr-1")
                .changeType(LedgerTransactionChangeType.CREATED)
                .after(expenseSnapshot("tx-1", LocalDate.of(2026, 2, 10), "MANUAL"))
                .build();

        when(budgetUsageQueryPort.findMonthlyBudgetUsages("usr-1", YearMonth.of(2026, 2)))
                .thenReturn(Collections.singletonList(new BudgetUsageSnapshot(
                        "budget-1",
                        YearMonth.of(2026, 2),
                        null,
                        "전체 예산",
                        100_000L,
                        110_000L,
                        110
                )));
        when(dispatchLogRepository.appendIfAbsent(any())).thenReturn(true);

        handler.handleLedgerTransactionChanged(event);

        verify(notificationCommandService, times(2)).createInApp(
                eq("usr-1"),
                any(),
                anyString(),
                anyString(),
                eq("BUDGET"),
                eq("budget-1")
        );
    }

    @Test
    public void shouldPublishFixedExpenseNotificationForAutoCreatedTransaction() {
        LedgerTransactionChangedEvent event = LedgerTransactionChangedEvent.builder()
                .ownerUserId("usr-1")
                .changeType(LedgerTransactionChangeType.CREATED)
                .after(expenseSnapshot("tx-fixed", LocalDate.of(2026, 2, 25), "FIXED_COST_AUTO"))
                .build();

        when(budgetUsageQueryPort.findMonthlyBudgetUsages("usr-1", YearMonth.of(2026, 2)))
                .thenReturn(Collections.emptyList());
        when(dispatchLogRepository.appendIfAbsent(any())).thenReturn(true);

        handler.handleLedgerTransactionChanged(event);

        verify(notificationCommandService).createInApp(
                eq("usr-1"),
                eq(com.payv.notification.domain.model.NotificationType.FIXED_EXPENSE_AUTO_CREATED),
                anyString(),
                contains("자동 생성 거래"),
                eq("TRANSACTION"),
                eq("tx-fixed")
        );
    }

    @Test
    public void shouldNotPublishDuplicateWhenDispatchKeyAlreadyExists() {
        LedgerTransactionChangedEvent event = LedgerTransactionChangedEvent.builder()
                .ownerUserId("usr-1")
                .changeType(LedgerTransactionChangeType.CREATED)
                .after(expenseSnapshot("tx-1", LocalDate.of(2026, 2, 10), "MANUAL"))
                .build();

        when(budgetUsageQueryPort.findMonthlyBudgetUsages("usr-1", YearMonth.of(2026, 2)))
                .thenReturn(Collections.singletonList(new BudgetUsageSnapshot(
                        "budget-1",
                        YearMonth.of(2026, 2),
                        null,
                        "전체 예산",
                        100_000L,
                        60_000L,
                        60
                )));
        when(dispatchLogRepository.appendIfAbsent(any())).thenReturn(false);

        handler.handleLedgerTransactionChanged(event);

        verify(notificationCommandService, never()).createInApp(
                eq("usr-1"),
                eq(com.payv.notification.domain.model.NotificationType.BUDGET_THRESHOLD_50),
                anyString(),
                anyString(),
                eq("BUDGET"),
                eq("budget-1")
        );
    }

    @Test
    public void shouldUseBeforeAndAfterMonthsForExpenseUpdate() {
        LedgerTransactionChangedEvent event = LedgerTransactionChangedEvent.builder()
                .ownerUserId("usr-1")
                .changeType(LedgerTransactionChangeType.UPDATED)
                .before(expenseSnapshot("tx-1", LocalDate.of(2026, 1, 31), "MANUAL"))
                .after(expenseSnapshot("tx-1", LocalDate.of(2026, 2, 1), "MANUAL"))
                .build();

        when(budgetUsageQueryPort.findMonthlyBudgetUsages(anyString(), any(YearMonth.class)))
                .thenReturn(Collections.emptyList());

        handler.handleLedgerTransactionChanged(event);

        ArgumentCaptor<YearMonth> monthCaptor = ArgumentCaptor.forClass(YearMonth.class);
        verify(budgetUsageQueryPort, times(2)).findMonthlyBudgetUsages(eq("usr-1"), monthCaptor.capture());
        assertEquals(YearMonth.of(2026, 1), monthCaptor.getAllValues().get(0));
        assertEquals(YearMonth.of(2026, 2), monthCaptor.getAllValues().get(1));
    }

    private LedgerTransactionSnapshot expenseSnapshot(String txId, LocalDate date, String sourceType) {
        return LedgerTransactionSnapshot.builder()
                .transactionId(txId)
                .ownerUserId("usr-1")
                .transactionType("EXPENSE")
                .transactionDate(date)
                .amount(55_000L)
                .sourceType(sourceType)
                .build();
    }
}
