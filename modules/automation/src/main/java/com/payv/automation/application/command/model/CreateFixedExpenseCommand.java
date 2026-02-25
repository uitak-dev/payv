package com.payv.automation.application.command.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateFixedExpenseCommand {

    private final String name;
    private final long amount;
    private final String assetId;
    private final String categoryIdLevel1;
    private final String categoryIdLevel2;
    private final String memo;
    private final Integer dayOfMonth;
    private final boolean endOfMonth;
}
