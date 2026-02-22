package com.payv.ledger.application.exception;

import com.payv.common.error.BadRequestException;

public class InvalidTransferAssetPairException extends BadRequestException {

    public InvalidTransferAssetPairException(String message) {
        super("LEDGER-TRANSFER-400", message);
    }
}
