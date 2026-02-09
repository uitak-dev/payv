package com.payv.asset.infrastructure.persistence.mybatis.record;

import com.payv.asset.domain.model.Asset;
import com.payv.asset.domain.model.AssetId;
import com.payv.asset.domain.model.AssetType;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
public class AssetRecord {

    private String assetId;
    private String ownerUserId;
    private String name;
    private String assetType;
    private boolean isActive;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @Builder
    private AssetRecord(String assetId, String ownerUserId, String name, String assetType,
                        boolean isActive, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.assetId = assetId;
        this.ownerUserId = ownerUserId;
        this.name = name;
        this.assetType = assetType;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static AssetRecord toRecord(Asset asset) {
        return AssetRecord.builder()
                .assetId(asset.getId().getValue())
                .ownerUserId(asset.getOwnerUserId())
                .name(asset.getName())
                .assetType(asset.getAssetType().name())
                .isActive(asset.isActive())
                .build();
    }

    public Asset toEntity() {
        return Asset.of(
                AssetId.of(assetId),
                ownerUserId,
                name,
                AssetType.valueOf(assetType),
                Boolean.TRUE.equals(isActive)
        );
    }
}
