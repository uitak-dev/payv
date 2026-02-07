package com.payv.asset.application.command.model;

import com.payv.asset.domain.model.AssetId;
import com.payv.asset.domain.model.AssetType;
import lombok.Getter;

@Getter
public class UpdateAssetCommand {

    private final AssetId assetId;
    private final String newName;
    private final AssetType assetType;

    public UpdateAssetCommand(AssetId assetId, String newName, AssetType assetType) {
        this.assetId = assetId;
        this.newName = newName;
        this.assetType = assetType;
    }
}
