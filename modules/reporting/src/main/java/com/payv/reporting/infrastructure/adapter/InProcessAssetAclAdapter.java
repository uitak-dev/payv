package com.payv.reporting.infrastructure.adapter;

import com.payv.contracts.asset.AssetPublicApi;
import com.payv.contracts.common.dto.IdNamePublicDto;
import com.payv.reporting.application.port.AssetLookupPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component("reportingAssetAclAdapter")
@RequiredArgsConstructor
public class InProcessAssetAclAdapter implements AssetLookupPort {

    private final AssetPublicApi publicService;

    @Override
    public List<IdNamePublicDto> getAssetNames(Collection<String> assetIds, String ownerUserId) {
        if (assetIds == null || assetIds.isEmpty()) return Collections.emptyList();
        return publicService.getAssetsByIds(ownerUserId, assetIds);
    }
}
