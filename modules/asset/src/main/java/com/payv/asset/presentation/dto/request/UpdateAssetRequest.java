package com.payv.asset.presentation.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public final class UpdateAssetRequest {

    @NotBlank
    private String newName;

    @NotBlank
    private String assetType;
}
