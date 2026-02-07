package com.payv.asset.domain.model;

import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

@Getter
public final class AssetId {

    private final String value;

    private AssetId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("assetId must not be blank");
        }
        this.value = value;
    }

    public static AssetId of(String value) {
        return new AssetId(value);
    }

    public static AssetId generate() {
        return new AssetId(UUID.randomUUID().toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AssetId)) return false;
        AssetId assetId = (AssetId) o;
        return Objects.equals(value, assetId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
