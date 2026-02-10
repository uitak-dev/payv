package com.payv.budget.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

@Getter
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class BudgetId {

    private final String value;

    public static BudgetId generate() {
        return new BudgetId(UUID.randomUUID().toString());
    }

    public static BudgetId of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("budgetId must not be blank");
        }
        return new BudgetId(value);
    }
}
