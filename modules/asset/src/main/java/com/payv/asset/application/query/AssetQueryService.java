package com.payv.asset.application.query;

import com.payv.asset.domain.model.Asset;
import com.payv.asset.domain.model.AssetId;
import com.payv.asset.domain.repository.AssetRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssetQueryService {

    private final AssetRepository assetRepository;

    public List<AssetView> getAll(String ownerUserId) {
        List<Asset> assets = assetRepository.findAllByOwner(ownerUserId);
        return assets.stream().map(AssetView::from).collect(Collectors.toList());
    }

    public Optional<AssetView> get(AssetId assetId, String ownerUserId) {
        return assetRepository.findById(assetId, ownerUserId).map(AssetView::from);
    }

    public Map<AssetId, String> getNamesByIds(String ownerUserId, Collection<AssetId> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyMap();
        return assetRepository.findNamesByIds(ownerUserId, ids);
    }

    @Getter
    public static class AssetView {
        private final String assetId;
        private final String name;
        private final String assetType;

        private AssetView(String assetId, String name, String assetType) {
            this.assetId = assetId;
            this.name = name;
            this.assetType = assetType;
        }

        public static AssetView from(Asset asset) {
            return new AssetView(
                    asset.getId().getValue(),
                    asset.getName(),
                    asset.getAssetType().name()
            );
        }
    }
}
