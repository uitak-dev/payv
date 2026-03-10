package com.payv.asset.application.query;

import com.payv.asset.application.query.model.AssetView;
import com.payv.asset.domain.model.Asset;
import com.payv.asset.domain.model.AssetId;
import com.payv.asset.domain.model.AssetType;
import com.payv.asset.domain.repository.AssetRepository;
import com.payv.contracts.common.dto.IdNamePublicDto;
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
import static org.junit.Assert.assertTrue;

public class AssetQueryServiceTest {

    private static final String OWNER = "user-1";

    private InMemoryAssetRepository repository;
    private AssetQueryService service;
    private AssetPublicService publicService;

    @Before
    public void setUp() {
        repository = new InMemoryAssetRepository();
        service = new AssetQueryService(repository);
        publicService = new AssetPublicService(repository);
    }

    @Test
    public void getAll_returnsActiveAssets() {
        // Given
        repository.save(Asset.create(OWNER, "현금", AssetType.CASH), OWNER);
        repository.save(Asset.create(OWNER, "신한카드", AssetType.CARD), OWNER);

        // When
        List<AssetView> result = service.getAll(OWNER);

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

        Set<String> ids = new LinkedHashSet<>();
        ids.add(cash.getId().getValue());
        ids.add("missing-id");

        // When
        List<IdNamePublicDto> rows = publicService.getAssetsByIds(OWNER, ids);

        // Then
        assertEquals(1, rows.size());
        assertEquals(cash.getId().getValue(), rows.get(0).getId());
        assertEquals("현금", rows.get(0).getName());
    }

    @Test
    public void getAssetsByOwner_returnsAllActiveAssetsAsDto() {
        // Given
        Asset cash = Asset.create(OWNER, "현금", AssetType.CASH);
        Asset card = Asset.create(OWNER, "카드", AssetType.CARD);
        repository.save(cash, OWNER);
        repository.save(card, OWNER);

        // When
        List<IdNamePublicDto> rows = publicService.getAssetsByOwner(OWNER);

        // Then
        assertEquals(2, rows.size());
        assertEquals(cash.getId().getValue(), rows.get(0).getId());
        assertEquals("현금", rows.get(0).getName());
        assertTrue(rows.stream().anyMatch(row -> row.getId().equals(card.getId().getValue())));
    }

    @Test
    public void get_returnsEmptyWhenInactive() {
        // Given
        Asset asset = Asset.create(OWNER, "비활성카드", AssetType.CARD);
        repository.save(asset, OWNER);
        asset.deactivate();
        repository.save(asset, OWNER);

        // When
        Optional<AssetView> result = service.get(asset.getId(), OWNER);

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
        public List<Asset> findNamesByIds(String ownerUserId, Collection<AssetId> assetIds) {
            if (assetIds == null || assetIds.isEmpty()) return Collections.emptyList();

            List<Asset> result = new ArrayList<>();
            for (Asset asset : findAllByOwner(ownerUserId)) {
                if (assetIds.contains(asset.getId())) {
                    result.add(asset);
                }
            }
            return result;
        }

        @Override
        public List<Asset> findNamesByOwner(String ownerUserId) {
            if (ownerUserId == null || ownerUserId.trim().isEmpty()) {
                return Collections.emptyList();
            }

            return findAllByOwner(ownerUserId);
        }
    }
}
