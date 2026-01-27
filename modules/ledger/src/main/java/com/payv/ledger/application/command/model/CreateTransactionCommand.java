package com.payv.ledger.application.command.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CreateTransactionCommand {

    private final String memo;
    private final LocalDate transactionDate;
}
