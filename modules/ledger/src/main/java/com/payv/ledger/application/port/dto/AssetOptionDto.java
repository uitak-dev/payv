package com.payv.ledger.application.port.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AssetOptionDto {
    private final String assetId;
    private final String name;
}
