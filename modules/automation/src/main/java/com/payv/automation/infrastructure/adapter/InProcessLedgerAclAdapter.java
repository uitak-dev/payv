package com.payv.automation.infrastructure.adapter;

import com.payv.automation.application.port.LedgerTransactionPort;
import com.payv.automation.domain.model.FixedExpenseExecution;
import com.payv.ledger.application.command.TransactionCommandService;
import com.payv.ledger.application.command.model.CreateAutoFixedExpenseTransactionCommand;
import com.payv.ledger.domain.model.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("automationLedgerAclAdapter")
@RequiredArgsConstructor
public class InProcessLedgerAclAdapter implements LedgerTransactionPort {

    private final TransactionCommandService transactionCommandService;

    @Override
    public String createFixedExpenseAutoTransaction(FixedExpenseExecution execution, String ownerUserId) {
        CreateAutoFixedExpenseTransactionCommand command = new CreateAutoFixedExpenseTransactionCommand(
                execution.getDefinitionId().getValue(),
                Money.generate(execution.getAmount()),
                execution.getScheduledDate(),
                execution.getAssetId(),
                execution.getCategoryIdLevel1(),
                execution.getCategoryIdLevel2(),
                execution.getMemo()
        );
        return transactionCommandService.createFixedCostAuto(command, ownerUserId).getValue();
    }
}
