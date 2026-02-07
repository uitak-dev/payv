package com.payv.asset.domain.repository;

import com.payv.asset.domain.model.Asset;
import com.payv.asset.domain.model.AssetId;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AssetRepository {

    void save(Asset asset, String ownerUserId);

    Optional<Asset> findById(AssetId assetId, String ownerUserId);

    List<Asset> findAllByOwner(String ownerUserId);

    Map<AssetId, String> findNamesByIds(String ownerUserId, Collection<AssetId> assetIds);
}
