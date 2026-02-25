package com.payv.automation.infrastructure.adapter;

import com.payv.asset.application.query.AssetQueryService;
import com.payv.asset.domain.model.AssetId;
import com.payv.automation.application.exception.InvalidFixedExpenseReferenceException;
import com.payv.automation.application.port.AssetQueryPort;
import com.payv.automation.application.port.AssetValidationPort;
import com.payv.automation.application.port.dto.AssetOptionDto;
import com.payv.common.error.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("automationAssetAclAdapter")
@RequiredArgsConstructor
public class InProcessAssetAclAdapter implements AssetValidationPort, AssetQueryPort {

    private final AssetQueryService assetQueryService;

    @Override
    public void validateAssetIds(Collection<String> assetIds, String ownerUserId) {
        if (assetIds == null || assetIds.isEmpty()) {
            return;
        }

        Set<String> normalized = new LinkedHashSet<>();
        for (String assetId : assetIds) {
            if (assetId == null || assetId.trim().isEmpty()) {
                throw new InvalidRequestException("assetId must not be blank");
            }
            normalized.add(assetId.trim());
        }

        Map<String, String> assetNames = getAssetNames(normalized, ownerUserId);
        if (assetNames.size() != normalized.size()) {
            throw new InvalidFixedExpenseReferenceException("invalid or inactive asset included");
        }
    }

    @Override
    public Map<String, String> getAssetNames(Collection<String> assetIds, String ownerUserId) {
        if (assetIds == null || assetIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<AssetId> ids = new LinkedHashSet<>();
        for (String assetId : assetIds) {
            if (assetId == null || assetId.trim().isEmpty()) {
                continue;
            }
            ids.add(AssetId.of(assetId.trim()));
        }
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<AssetId, String> fetched = assetQueryService.getNamesByIds(ownerUserId, ids);
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<AssetId, String> entry : fetched.entrySet()) {
            result.put(entry.getKey().getValue(), entry.getValue());
        }
        return result;
    }

    @Override
    public List<AssetOptionDto> getAllAssets(String ownerUserId) {
        List<AssetQueryService.AssetView> rows = assetQueryService.getAll(ownerUserId);
        List<AssetOptionDto> result = new ArrayList<>(rows.size());
        for (AssetQueryService.AssetView row : rows) {
            result.add(new AssetOptionDto(row.getAssetId(), row.getName()));
        }
        return result;
    }
}
