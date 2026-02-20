package com.payv.reporting.infrastructure.adapter;

import com.payv.asset.application.query.AssetQueryService;
import com.payv.asset.domain.model.AssetId;
import com.payv.reporting.application.port.AssetLookupPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Component("reportingAssetAclAdapter")
@RequiredArgsConstructor
public class InProcessAssetAclAdapter implements AssetLookupPort {

    private final AssetQueryService assetQueryService;

    @Override
    public Map<String, String> getAssetNames(Collection<String> assetIds, String ownerUserId) {
        if (assetIds == null || assetIds.isEmpty()) return Collections.emptyMap();

        Set<AssetId> ids = new LinkedHashSet<>();
        for (String assetId : assetIds) {
            if (assetId == null || assetId.trim().isEmpty()) continue;
            ids.add(AssetId.of(assetId));
        }
        if (ids.isEmpty()) return Collections.emptyMap();

        Map<AssetId, String> fetched = assetQueryService.getNamesByIds(ownerUserId, ids);
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<AssetId, String> entry : fetched.entrySet()) {
            result.put(entry.getKey().getValue(), entry.getValue());
        }
        return result;
    }
}
