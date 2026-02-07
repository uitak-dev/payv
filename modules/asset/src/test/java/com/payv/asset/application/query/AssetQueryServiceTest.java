package com.payv.asset.application.query;

import com.payv.asset.domain.model.Asset;
import com.payv.asset.domain.model.AssetId;
import com.payv.asset.domain.model.AssetType;
import com.payv.asset.domain.repository.AssetRepository;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class AssetQueryServiceTest {

    private static final String OWNER = "user-1";

    private InMemoryAssetRepository repository;
    private AssetQueryService service;

    @Before
    public void setUp() {
        repository = new InMemoryAssetRepository();
        service = new AssetQueryService(repository);
    }

    @Test
    public void getAll_returnsActiveAssets() {
        // Given
        repository.save(Asset.create(OWNER, "현금", AssetType.CASH), OWNER);
        repository.save(Asset.create(OWNER, "신한카드", AssetType.CARD), OWNER);

        // When
        List<AssetQueryService.AssetView> result = service.getAll(OWNER);

        // Then
        assertEquals(2, result.size());
        assertEquals("현금", result.get(0).getName());
        assertEquals("CASH", result.get(0).getAssetType());
    }

    @Test
    public void getNamesByIds_returnsMatchedNames() {
        // Given
        Asset cash = Asset.create(OWNER, "현금", AssetType.CASH);
        Asset card = Asset.create(OWNER, "카드", AssetType.CARD);
        repository.save(cash, OWNER);
        repository.save(card, OWNER);

        Set<AssetId> ids = new LinkedHashSet<>();
        ids.add(cash.getId());
        ids.add(AssetId.of("missing-id"));

        // When
        Map<AssetId, String> names = service.getNamesByIds(OWNER, ids);

        // Then
        assertEquals(1, names.size());
        assertEquals("현금", names.get(cash.getId()));
    }

    @Test
    public void get_returnsEmptyWhenInactive() {
        // Given
        Asset asset = Asset.create(OWNER, "비활성카드", AssetType.CARD);
        repository.save(asset, OWNER);
        asset.deactivate();
        repository.save(asset, OWNER);

        // When
        Optional<AssetQueryService.AssetView> result = service.get(asset.getId(), OWNER);

        // Then
        assertFalse(result.isPresent());
    }

    private static class InMemoryAssetRepository implements AssetRepository {

        private final Map<String, Map<AssetId, Asset>> storeByOwner = new LinkedHashMap<>();

        @Override
        public void save(Asset asset, String ownerUserId) {
            storeByOwner
                    .computeIfAbsent(ownerUserId, k -> new LinkedHashMap<>())
                    .put(asset.getId(), asset);
        }

        @Override
        public Optional<Asset> findById(AssetId assetId, String ownerUserId) {
            Map<AssetId, Asset> assets = storeByOwner.get(ownerUserId);
            if (assets == null) return Optional.empty();

            Asset asset = assets.get(assetId);
            if (asset == null || !asset.isActive()) return Optional.empty();
            return Optional.of(asset);
        }

        @Override
        public List<Asset> findAllByOwner(String ownerUserId) {
            Map<AssetId, Asset> assets = storeByOwner.get(ownerUserId);
            if (assets == null) return Collections.emptyList();

            List<Asset> result = new ArrayList<>();
            for (Asset asset : assets.values()) {
                if (asset.isActive()) {
                    result.add(asset);
                }
            }
            return result;
        }

        @Override
        public Map<AssetId, String> findNamesByIds(String ownerUserId, Collection<AssetId> assetIds) {
            if (assetIds == null || assetIds.isEmpty()) return Collections.emptyMap();

            Map<AssetId, String> result = new LinkedHashMap<>();
            for (Asset asset : findAllByOwner(ownerUserId)) {
                if (assetIds.contains(asset.getId())) {
                    result.put(asset.getId(), asset.getName());
                }
            }
            return result;
        }
    }
}
