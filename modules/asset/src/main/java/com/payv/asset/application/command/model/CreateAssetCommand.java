package com.payv.asset.application.command.model;

import com.payv.asset.domain.model.AssetType;
import lombok.Getter;

@Getter
public class CreateAssetCommand {

    private final String name;
    private final AssetType assetType;

    public CreateAssetCommand(String name, AssetType assetType) {
        this.name = name;
        this.assetType = assetType;
    }
}
