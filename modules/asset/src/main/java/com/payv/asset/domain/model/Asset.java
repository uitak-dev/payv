package com.payv.asset.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.util.Objects;

@Getter
public class Asset {

    private static final int MAX_NAME_LENGTH = 20;

    private AssetId id;
    private String ownerUserId;
    private String name;
    private AssetType assetType;
    private boolean isActive;

    @Builder
    private Asset(AssetId id, String ownerUserId, String name,
                  AssetType assetType, boolean isActive) {
        this.id = id;
        this.ownerUserId = ownerUserId;
        this.name = name;
        this.assetType = assetType;
        this.isActive = isActive;
    }

    public static Asset create(String ownerUserId, String name, AssetType assetType) {
        return Asset.builder()
                .id(AssetId.generate())
                .ownerUserId(ownerUserId)
                .name(normalizeName(name))
                .assetType(requireAssetType(assetType))
                .isActive(true)
                .build();
    }

    public static Asset of(AssetId id, String ownerUserId, String name,
                           AssetType assetType, boolean isActive) {
        return Asset.builder()
                .id(id)
                .ownerUserId(ownerUserId)
                .name(name)
                .assetType(assetType)
                .isActive(isActive)
                .build();
    }

    public void rename(String newName) {
        requireActive();
        this.name = normalizeName(newName);
    }

    public void changeType(AssetType newType) {
        requireActive();
        this.assetType = requireAssetType(newType);
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void ensureBelongsTo(String requesterOwnerUserId) {
        if (!Objects.equals(this.ownerUserId, requesterOwnerUserId)) {
            throw new IllegalStateException("asset owner mismatch");
        }
    }

    private void requireActive() {
        if (!isActive) {
            throw new IllegalStateException("inactive asset");
        }
    }

    private static AssetType requireAssetType(AssetType assetType) {
        if (assetType == null) {
            throw new IllegalArgumentException("assetType must not be null");
        }
        return assetType;
    }

    public static String normalizeName(String name) {
        String ret = (name == null) ? null : name.trim();
        if (ret == null || ret.isEmpty()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (ret.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("asset name length must be <= " + MAX_NAME_LENGTH);
        }
        return ret;
    }
}
