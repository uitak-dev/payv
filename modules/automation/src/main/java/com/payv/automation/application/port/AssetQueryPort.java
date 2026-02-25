package com.payv.automation.application.port;

import com.payv.automation.application.port.dto.AssetOptionDto;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface AssetQueryPort {

    Map<String, String> getAssetNames(Collection<String> assetIds, String ownerUserId);

    List<AssetOptionDto> getAllAssets(String ownerUserId);
}
