package com.payv.automation.presentation.dto.request;

import com.payv.automation.application.command.model.CreateFixedExpenseCommand;
import lombok.Data;

@Data
public class CreateFixedExpenseRequest {

    private String name;
    private Long amount;
    private String assetId;
    private String categoryIdLevel1;
    private String categoryIdLevel2;
    private String memo;
    private Integer dayOfMonth;
    private String scheduleType;

    public CreateFixedExpenseCommand toCommand() {
        boolean endOfMonth = "EOM".equalsIgnoreCase(scheduleType);
        return new CreateFixedExpenseCommand(
                name,
                amount == null ? 0L : amount,
                assetId,
                categoryIdLevel1,
                normalizeNullable(categoryIdLevel2),
                memo,
                endOfMonth ? null : dayOfMonth,
                endOfMonth
        );
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
