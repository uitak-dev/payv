package com.payv.ledger.application.port;

import java.util.Collection;

public interface AssetValidationPort {

    void validateAssetIds(Collection<String> assetIds, String ownerUserId);
}
