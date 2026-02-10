package com.payv.asset.presentation.dto.request;

import com.payv.asset.application.command.model.UpdateAssetCommand;
import com.payv.asset.domain.model.AssetId;
import com.payv.asset.domain.model.AssetType;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public final class UpdateAssetRequest {

    @NotBlank
    private String newName;

    @NotBlank
    private String assetType;

    public UpdateAssetCommand toCommand(String assetId) {
        return new UpdateAssetCommand(AssetId.of(assetId), newName, AssetType.valueOf(assetType));
    }
}
