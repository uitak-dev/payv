package com.payv.ledger.domain.model;

import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

@Getter
public final class TransferId {

    private final String value;

    private TransferId(String value) {
        this.value = value;
    }

    public static TransferId generate() {
        return new TransferId(UUID.randomUUID().toString());
    }

    public static TransferId of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("transferId must not be blank");
        }
        return new TransferId(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransferId)) return false;
        TransferId that = (TransferId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
