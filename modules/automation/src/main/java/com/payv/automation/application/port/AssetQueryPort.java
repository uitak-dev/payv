package com.payv.automation.application.port;

import com.payv.contracts.common.dto.IdNamePublicDto;

import java.util.Collection;
import java.util.List;

public interface AssetQueryPort {

    List<IdNamePublicDto> getAssetNames(Collection<String> assetIds, String ownerUserId);

    List<IdNamePublicDto> getAllAssets(String ownerUserId);
}
