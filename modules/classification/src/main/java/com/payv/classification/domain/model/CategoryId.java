package com.payv.classification.domain.model;

import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

@Getter
public final class CategoryId {

    private final String value;

    private CategoryId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("categoryId must not be blank");
        }
        this.value = value;
    }

    public static CategoryId of(String value) {
        return new CategoryId(value);
    }

    public static CategoryId generate() {
        return new CategoryId(UUID.randomUUID().toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CategoryId)) return false;
        CategoryId categoryId = (CategoryId) o;
        return Objects.equals(value, categoryId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
