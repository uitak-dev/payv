package com.payv.budget.presentation.dto.request;

import lombok.Data;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;

@Data
public final class BudgetListConditionRequest {

    private String month;

    public YearMonth resolvedMonth() {
        String raw = month == null ? null : month.trim();
        if (raw == null || raw.isEmpty()) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(raw);
        } catch (DateTimeParseException e) {
            return YearMonth.now();
        }
    }
}
