package com.payv.classification.domain.model;

import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

@Getter
public final class TagId {

    private final String value;

    private TagId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("tagId must not be blank");
        }
        this.value = value;
    }

    public static TagId of(String value) {
        return new TagId(value);
    }

    public static TagId generate() {
        return new TagId(UUID.randomUUID().toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TagId)) return false;
        TagId tagId = (TagId) o;
        return Objects.equals(value, tagId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
