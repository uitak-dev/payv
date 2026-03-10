package com.payv.asset.domain.repository;

import com.payv.asset.domain.model.Asset;
import com.payv.asset.domain.model.AssetId;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AssetRepository {

    void save(Asset asset, String ownerUserId);
    Optional<Asset> findById(AssetId assetId, String ownerUserId);
    List<Asset> findAllByOwner(String ownerUserId);

    // ---- Public Service(다른 BC에 노출)를 위한, 메서드 ----
    List<Asset> findNamesByIds(String ownerUserId, Collection<AssetId> assetIds);
    List<Asset> findNamesByOwner(String ownerUserId);
}
