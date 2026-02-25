package com.payv.automation.application.query.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FixedExpenseView {

    private final String definitionId;
    private final String name;
    private final long amount;
    private final String assetId;
    private final String assetName;
    private final String categoryIdLevel1;
    private final String categoryNameLevel1;
    private final String categoryIdLevel2;
    private final String categoryNameLevel2;
    private final String memo;
    private final Integer dayOfMonth;
    private final boolean endOfMonth;

    public String getScheduleLabel() {
        if (endOfMonth) {
            return "매월 말일";
        }
        return "매월 " + dayOfMonth + "일";
    }
}
