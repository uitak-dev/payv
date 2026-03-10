package com.payv.contracts.asset;

import com.payv.contracts.common.dto.IdNamePublicDto;

import java.util.Collection;
import java.util.List;

public interface AssetPublicApi {

    List<IdNamePublicDto> getAssetsByIds(String ownerUserId, Collection<String> assetIds);

    List<IdNamePublicDto> getAssetsByOwner(String ownerUserId);
}
