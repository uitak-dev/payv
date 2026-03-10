package com.payv.asset.application.query.model;

import com.payv.asset.domain.model.Asset;
import lombok.Getter;

@Getter
public class AssetView {
    private final String assetId;
    private final String name;
    private final String assetType;

    private AssetView(String assetId, String name, String assetType) {
        this.assetId = assetId;
        this.name = name;
        this.assetType = assetType;
    }

    public static AssetView from(Asset asset) {
        return new AssetView(
                asset.getId().getValue(),
                asset.getName(),
                asset.getAssetType().name()
        );
    }
}
