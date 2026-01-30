package com.payv.ledger.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.util.Objects;

@Getter
public final class TransactionSource {

    private final TransactionSourceType type;
    private final String originalReference;

    @Builder
    private TransactionSource(TransactionSourceType type, String originalReference) {
        this.type = type;
        this.originalReference = originalReference;
    }

    public static TransactionSource manual() {
        return new TransactionSource(TransactionSourceType.MANUAL, null);
    }

    public static TransactionSource fixedCost(String fixedCostTemplateId) {
        if (fixedCostTemplateId == null || fixedCostTemplateId.trim().isEmpty()) {
            throw new IllegalArgumentException("fixedCostTemplateId is required");
        }
        return new TransactionSource(TransactionSourceType.FIXED_COST_AUTO, fixedCostTemplateId);
    }

    public static TransactionSource of(TransactionSourceType type, String originalReference) {
        Objects.requireNonNull(type, "TransactionSourceType is required");
        return new TransactionSource(type, originalReference);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransactionSource)) return false;
        TransactionSource that = (TransactionSource) o;
        return type == that.type && Objects.equals(originalReference, that.originalReference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, originalReference);
    }
}
