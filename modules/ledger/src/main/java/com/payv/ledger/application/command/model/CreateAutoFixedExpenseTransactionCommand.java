package com.payv.ledger.application.command.model;

import com.payv.ledger.domain.model.Money;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class CreateAutoFixedExpenseTransactionCommand {

    private final String fixedExpenseDefinitionId;
    private final Money amount;
    private final LocalDate transactionDate;
    private final String assetId;
    private final String categoryIdLevel1;
    private final String categoryIdLevel2;
    private final String memo;
}
