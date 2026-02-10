package com.payv.ledger.infrastructure.adapter;

import com.payv.asset.application.query.AssetQueryService;
import com.payv.asset.domain.model.AssetId;
import com.payv.ledger.application.port.AssetQueryPort;
import com.payv.ledger.application.port.AssetValidationPort;
import com.payv.ledger.application.port.dto.AssetOptionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class InProcessAssetAclAdapter implements AssetValidationPort, AssetQueryPort {

    private final AssetQueryService assetQueryService;

    @Override
    public void validateAssertId(String assetId, String ownerUserId) {
        if (assetId == null || assetId.trim().isEmpty()) {
            throw new IllegalArgumentException("assetId must not be blank");
        }

        boolean exists = assetQueryService.get(AssetId.of(assetId), ownerUserId).isPresent();
        if (!exists) {
            throw new IllegalStateException("invalid or inactive asset included");
        }
    }

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

    @Override
    public List<AssetOptionDto> getAllActiveAssets(String ownerUserId) {
        return assetQueryService.getAll(ownerUserId).stream()
                .map(asset -> new AssetOptionDto(asset.getAssetId(), asset.getName()))
                .collect(Collectors.toList());
    }
}
