package com.payv.ledger.application.publicapi;

import com.payv.contracts.ledger.LedgerPublicApi;
import com.payv.contracts.ledger.dto.CreateFixedExpenseAutoTransactionPublicRequest;
import com.payv.ledger.application.command.TransactionCommandService;
import com.payv.ledger.application.command.model.CreateAutoFixedExpenseTransactionCommand;
import com.payv.ledger.application.query.TransactionQueryService;
import com.payv.ledger.domain.model.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LedgerPublicService implements LedgerPublicApi {

    private final TransactionQueryService transactionQueryService;
    private final TransactionCommandService transactionCommandService;

    @Override
    public long sumExpenseAmount(String ownerUserId,
                                 LocalDate from,
                                 LocalDate to,
                                 String categoryIdLevel1,
                                 String categoryIdLevel2) {
        return transactionQueryService.sumExpenseAmount(
                ownerUserId, from, to, categoryIdLevel1, categoryIdLevel2
        );
    }

    @Transactional
    @Override
    public String createFixedExpenseAutoTransaction(String ownerUserId,
                                                    CreateFixedExpenseAutoTransactionPublicRequest request) {
        Objects.requireNonNull(request, "request");

        CreateAutoFixedExpenseTransactionCommand command = new CreateAutoFixedExpenseTransactionCommand(
                request.getFixedExpenseDefinitionId(),
                Money.generate(request.getAmount()),
                request.getTransactionDate(),
                request.getAssetId(),
                request.getCategoryIdLevel1(),
                request.getCategoryIdLevel2(),
                request.getMemo()
        );

        return transactionCommandService.createFixedCostAuto(command, ownerUserId).getValue();
    }
}
