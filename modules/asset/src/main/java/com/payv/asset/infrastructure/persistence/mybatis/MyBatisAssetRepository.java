package com.payv.asset.infrastructure.persistence.mybatis;

import com.payv.asset.domain.model.Asset;
import com.payv.asset.domain.model.AssetId;
import com.payv.asset.domain.repository.AssetRepository;
import com.payv.asset.infrastructure.persistence.mybatis.mapper.AssetMapper;
import com.payv.asset.infrastructure.persistence.mybatis.record.AssetRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class MyBatisAssetRepository implements AssetRepository {

    private final AssetMapper assetMapper;

    @Override
    public void save(Asset asset, String ownerUserId) {
        assetMapper.upsert(AssetRecord.toRecord(asset));
    }

    @Override
    public Optional<Asset> findById(AssetId assetId, String ownerUserId) {
        AssetRecord record = assetMapper.selectByIdAndOwner(assetId.getValue(), ownerUserId);
        if (record == null) return Optional.empty();
        return Optional.of(record.toEntity());
    }

    @Override
    public List<Asset> findAllByOwner(String ownerUserId) {
        List<AssetRecord> records = assetMapper.selectAllByOwner(ownerUserId);
        if (records == null || records.isEmpty()) return Collections.emptyList();
        return records.stream().map(AssetRecord::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<Asset> findNamesByIds(String ownerUserId, Collection<AssetId> assetIds) {
        if (assetIds == null || assetIds.isEmpty()) return Collections.emptyList();

        List<String> ids = assetIds.stream()
                .filter(Objects::nonNull)
                .map(AssetId::getValue)
                .collect(Collectors.toList());
        if (ids.isEmpty()) return Collections.emptyList();

        List<AssetRecord> rows = assetMapper.selectNamesByIds(ownerUserId, ids);
        if (rows == null || rows.isEmpty()) return Collections.emptyList();
        return rows.stream().map(AssetRecord::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<Asset> findNamesByOwner(String ownerUserId) {
        if (ownerUserId == null || ownerUserId.isEmpty()) return Collections.emptyList();

        List<AssetRecord> rows = assetMapper.selectNamesByOwner(ownerUserId);
        if (rows == null || rows.isEmpty()) return Collections.emptyList();
        return rows.stream().map(AssetRecord::toEntity).collect(Collectors.toList());
    }

}
