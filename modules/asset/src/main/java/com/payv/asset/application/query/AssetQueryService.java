package com.payv.asset.application.query;

import com.payv.asset.application.query.model.AssetView;
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
/**
 * Asset BC의 조회 기능을 제공하는 읽기 전용 서비스.
 * - 자산 목록/상세/ID 기반 이름 맵 조회를 제공한다.
 */
public class AssetQueryService {

    private final AssetRepository assetRepository;

    /**
     * 소유자의 전체 자산을 조회한다.
     *
     * @param ownerUserId 소유 사용자 ID
     * @return 자산 뷰 목록
     */
    public List<AssetView> getAll(String ownerUserId) {
        List<Asset> assets = assetRepository.findAllByOwner(ownerUserId);
        return assets.stream().map(AssetView::from).collect(Collectors.toList());
    }

    /**
     * 자산 단건을 조회한다.
     *
     * @param assetId 자산 ID
     * @param ownerUserId 소유 사용자 ID
     * @return 자산 뷰. 없으면 {@link Optional#empty()}
     */
    public Optional<AssetView> get(AssetId assetId, String ownerUserId) {
        return assetRepository.findById(assetId, ownerUserId).map(AssetView::from);
    }

}
