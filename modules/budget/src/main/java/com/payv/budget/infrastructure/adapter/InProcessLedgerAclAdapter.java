package com.payv.budget.infrastructure.adapter;

import com.payv.budget.application.port.LedgerSpendingQueryPort;
import com.payv.contracts.ledger.LedgerPublicApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class InProcessLedgerAclAdapter implements LedgerSpendingQueryPort {

    private final LedgerPublicApi ledgerPublicService;

    @Override
    public long sumExpenseAmount(String ownerUserId, LocalDate from, LocalDate to,
                                 String categoryIdLevel1) {
        return ledgerPublicService.sumExpenseAmount(
                ownerUserId, from, to, categoryIdLevel1, null
        );
    }
}
