package com.payv.asset.application.exception;

import com.payv.common.error.NotFoundException;

public class AssetNotFoundException extends NotFoundException {

    public AssetNotFoundException() {
        super("ASSET-404", "asset not found");
    }
}
