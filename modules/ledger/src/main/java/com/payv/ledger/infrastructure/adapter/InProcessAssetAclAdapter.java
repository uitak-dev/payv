package com.payv.ledger.infrastructure.adapter;

import com.payv.ledger.application.port.AssetQueryPort;
import com.payv.ledger.application.port.AssetValidationPort;

import java.util.Collection;
import java.util.Map;

public class InProcessAssetAclAdapter implements AssetValidationPort, AssetQueryPort {
    @Override
    public void validateAssertId(String assetId, String ownerUserId) {

    }

    @Override
    public Map<String, String> getAssetNames(Collection<String> assetIds, String ownerUserId) {
        return null;
    }
}
