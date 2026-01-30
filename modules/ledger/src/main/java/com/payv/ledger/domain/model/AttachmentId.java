package com.payv.ledger.domain.model;

import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

@Getter
public final class AttachmentId {

    private final String value;

    private AttachmentId(String value) {
        this.value = value;
    }

    public static AttachmentId generate() {
        return new AttachmentId(UUID.randomUUID().toString());
    }

    public static AttachmentId of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("attachmentId must not be blank");
        }
        return new AttachmentId(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AttachmentId)) return false;
        AttachmentId that = (AttachmentId) o;
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
