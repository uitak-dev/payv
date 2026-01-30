package com.payv.ledger.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
public final class Money {

    private Long amount;

    @Builder
    private Money(Long amount) {
        this.amount = amount;
    }

    public static Money generate(Long amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
        return Money.builder()
                .amount(amount)
                .build();
    }

    public static Money of(Long amount) {
        return Money.builder()
                .amount(amount)
                .build();
    }
}
