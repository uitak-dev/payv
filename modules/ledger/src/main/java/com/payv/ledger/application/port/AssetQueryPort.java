package com.payv.ledger.application.port;

import java.util.Collection;
import java.util.Map;

public interface AssetQueryPort {
    Map<String, String> getAssetNames(Collection<String> assetIds, String ownerUserId);
}
