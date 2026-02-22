package com.payv.asset.application.exception;

import com.payv.common.error.ConflictException;

public class DuplicateAssetNameException extends ConflictException {

    public DuplicateAssetNameException() {
        super("ASSET-409", "duplicate asset name");
    }
}
