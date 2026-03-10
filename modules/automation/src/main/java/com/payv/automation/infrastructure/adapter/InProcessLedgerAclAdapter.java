package com.payv.automation.infrastructure.adapter;

import com.payv.automation.application.port.LedgerTransactionPort;
import com.payv.automation.domain.model.FixedExpenseExecution;
import com.payv.contracts.ledger.LedgerPublicApi;
import com.payv.contracts.ledger.dto.CreateFixedExpenseAutoTransactionPublicRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("automationLedgerAclAdapter")
@RequiredArgsConstructor
public class InProcessLedgerAclAdapter implements LedgerTransactionPort {

    private final LedgerPublicApi ledgerPublicService;

    @Override
    public String createFixedExpenseAutoTransaction(FixedExpenseExecution execution, String ownerUserId) {
        CreateFixedExpenseAutoTransactionPublicRequest request = new CreateFixedExpenseAutoTransactionPublicRequest(
                execution.getDefinitionId().getValue(),
                execution.getAmount(),
                execution.getScheduledDate(),
                execution.getAssetId(),
                execution.getCategoryIdLevel1(),
                execution.getCategoryIdLevel2(),
                execution.getMemo()
        );
        return ledgerPublicService.createFixedExpenseAutoTransaction(ownerUserId, request);
    }
}
