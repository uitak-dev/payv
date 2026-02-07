package com.payv.asset.application.command;

import com.payv.asset.application.command.model.CreateAssetCommand;
import com.payv.asset.application.command.model.DeactivateAssetCommand;
import com.payv.asset.application.command.model.UpdateAssetCommand;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

public class AssetCommandServiceTest {

    private static final String OWNER = "user-1";

    private InMemoryAssetRepository repository;
    private AssetCommandService service;

    @Before
    public void setUp() {
        repository = new InMemoryAssetRepository();
        service = new AssetCommandService(repository);
    }

    @Test
    public void create_createsAsset() {
        // Given
        CreateAssetCommand command = new CreateAssetCommand("생활비", AssetType.CASH);

        // When
        AssetId id = service.create(command, OWNER);

        // Then
        Asset saved = repository.findById(id, OWNER).orElse(null);
        assertNotNull(saved);
        assertEquals("생활비", saved.getName());
        assertEquals(AssetType.CASH, saved.getAssetType());
        assertTrue(saved.isActive());
    }

    @Test(expected = IllegalStateException.class)
    public void create_rejectsDuplicateName() {
        // Given
        repository.save(Asset.create(OWNER, "신한카드", AssetType.CARD), OWNER);

        // When
        service.create(new CreateAssetCommand("신한카드", AssetType.CARD), OWNER);
    }

    @Test
    public void update_updatesNameAndType() {
        // Given
        Asset asset = Asset.create(OWNER, "현금", AssetType.CASH);
        repository.save(asset, OWNER);

        // When
        service.update(new UpdateAssetCommand(asset.getId(), "우리은행", AssetType.BANK_ACCOUNT), OWNER);

        // Then
        Asset updated = repository.findById(asset.getId(), OWNER).orElse(null);
        assertNotNull(updated);
        assertEquals("우리은행", updated.getName());
        assertEquals(AssetType.BANK_ACCOUNT, updated.getAssetType());
    }

    @Test
    public void deactivate_setsInactive() {
        // Given
        Asset asset = Asset.create(OWNER, "삼성카드", AssetType.CARD);
        repository.save(asset, OWNER);

        // When
        service.deactivate(new DeactivateAssetCommand(asset.getId()), OWNER);

        // Then
        assertFalse(repository.findById(asset.getId(), OWNER).isPresent());
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
