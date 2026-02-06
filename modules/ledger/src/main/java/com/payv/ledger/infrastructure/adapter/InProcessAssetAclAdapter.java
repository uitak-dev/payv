package com.payv.ledger.infrastructure.adapter;

import com.payv.ledger.application.port.AssetQueryPort;
import com.payv.ledger.application.port.AssetValidationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class InProcessAssetAclAdapter implements AssetValidationPort, AssetQueryPort {

    /**
     * Asset BC 완성 후, AssetQueryService 의존성 추가 후, 각 기능 구현.
     */

    @Override
    public void validateAssertId(String assetId, String ownerUserId) {
        if (assetId == null || assetId.trim().isEmpty()) {
            throw new IllegalArgumentException("assetId must not be blank");
        }
    }

    @Override
    public Map<String, String> getAssetNames(Collection<String> assetIds, String ownerUserId) {
        return Collections.emptyMap();
    }
}
