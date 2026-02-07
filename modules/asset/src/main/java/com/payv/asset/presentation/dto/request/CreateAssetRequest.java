package com.payv.asset.presentation.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public final class CreateAssetRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String assetType;
}
