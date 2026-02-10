package com.payv.budget.presentation.dto.request;

import com.payv.budget.application.command.model.CreateBudgetCommand;
import lombok.Data;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;

@Data
public final class CreateBudgetRequest {

    private String month;
    private Long amountLimit;
    private String categoryId;
    private String memo;

    public CreateBudgetCommand toCommand() {
        return new CreateBudgetCommand(
                parseMonth(month),
                amountLimit == null ? 0L : amountLimit,
                normalizeNullable(categoryId),
                memo
        );
    }

    private YearMonth parseMonth(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("month must not be blank");
        }
        try {
            return YearMonth.parse(value.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("invalid month format. expected YYYY-MM");
        }
    }

    private String normalizeNullable(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
