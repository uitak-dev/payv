package com.payv.budget.application.command.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.YearMonth;

@Getter
@AllArgsConstructor
public class CreateBudgetCommand {
    private final YearMonth targetMonth;
    private final long amountLimit;
    private final String categoryId;
    private final String memo;
}
