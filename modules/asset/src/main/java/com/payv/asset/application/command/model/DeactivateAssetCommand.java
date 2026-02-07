package com.payv.asset.application.command.model;

import com.payv.asset.domain.model.AssetId;
import lombok.Getter;

@Getter
public class DeactivateAssetCommand {

    private final AssetId assetId;

    public DeactivateAssetCommand(AssetId assetId) {
        this.assetId = assetId;
    }
}
