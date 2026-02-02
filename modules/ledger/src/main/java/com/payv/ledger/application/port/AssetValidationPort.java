package com.payv.ledger.application.port;

public interface AssetValidationPort {
    void validateAssertId(String assetId, String ownerUserId);
}
