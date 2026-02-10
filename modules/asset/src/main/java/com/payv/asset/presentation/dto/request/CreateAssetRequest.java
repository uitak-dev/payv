package com.payv.asset.presentation.dto.request;

import com.payv.asset.application.command.model.CreateAssetCommand;
import com.payv.asset.domain.model.AssetType;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public final class CreateAssetRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String assetType;

    public CreateAssetCommand toCommand() {
        return new CreateAssetCommand(name, AssetType.valueOf(assetType));
    }
}
