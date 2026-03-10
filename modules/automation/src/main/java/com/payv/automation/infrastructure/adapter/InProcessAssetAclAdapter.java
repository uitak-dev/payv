package com.payv.automation.infrastructure.adapter;

import com.payv.automation.application.exception.InvalidFixedExpenseReferenceException;
import com.payv.automation.application.port.AssetQueryPort;
import com.payv.automation.application.port.AssetValidationPort;
import com.payv.contracts.asset.AssetPublicApi;
import com.payv.contracts.common.dto.IdNamePublicDto;
import com.payv.common.error.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component("automationAssetAclAdapter")
@RequiredArgsConstructor
public class InProcessAssetAclAdapter implements AssetValidationPort, AssetQueryPort {

    private final AssetPublicApi publicService;

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

        List<IdNamePublicDto> assets = getAssetNames(normalized, ownerUserId);
        if (assets.size() != normalized.size()) {
            throw new InvalidFixedExpenseReferenceException("invalid or inactive asset included");
        }
    }

    @Override
    public List<IdNamePublicDto> getAssetNames(Collection<String> assetIds, String ownerUserId) {
        if (assetIds == null || assetIds.isEmpty()) {
            return Collections.emptyList();
        }
        return publicService.getAssetsByIds(ownerUserId, assetIds);
    }

    @Override
    public List<IdNamePublicDto> getAllAssets(String ownerUserId) {
        return publicService.getAssetsByOwner(ownerUserId);
    }
}
