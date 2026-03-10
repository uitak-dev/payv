package com.payv.asset.application.query;

import com.payv.asset.domain.model.Asset;
import com.payv.asset.domain.model.AssetId;
import com.payv.asset.domain.repository.AssetRepository;
import com.payv.contracts.asset.AssetPublicApi;
import com.payv.contracts.common.dto.IdNamePublicDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
/**
 * Asset BC의 조회 기능을 제공하는 읽기 전용 서비스.
 * - Ledger/Reporting/Automation 등 다른 BC가 자산 정보를 조회할 때,
 *   Asset BC 내부 VO(AssetId) 노출 없이 DTO 계약으로 안정적으로 제공한다.
 */
public class AssetPublicService implements AssetPublicApi {

    private final AssetRepository assetRepository;

    /**
     * 자산 ID 목록에 해당하는 자산 DTO 목록을 반환한다.
     *
     * @param ownerUserId 소유 사용자 ID
     * @param assetIds 조회 대상 자산 ID 목록
     * @return 활성 자산 DTO 목록
     */
    @Override
    public List<IdNamePublicDto> getAssetsByIds(String ownerUserId, Collection<String> assetIds) {
        if (ownerUserId == null || ownerUserId.trim().isEmpty() || assetIds == null || assetIds.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> normalizedIds = new LinkedHashSet<>();
        for (String assetId : assetIds) {
            if (assetId == null || assetId.trim().isEmpty()) {
                continue;
            }
            normalizedIds.add(assetId.trim());
        }
        if (normalizedIds.isEmpty()) {
            return Collections.emptyList();
        }

        Set<AssetId> ids = new LinkedHashSet<>();
        for (String normalizedId : normalizedIds) {
            ids.add(AssetId.of(normalizedId));
        }

        List<Asset> fetched = assetRepository.findNamesByIds(ownerUserId, ids);

        // 요청 ids 목록 순서와 db 조회 후, 반환되는 데이터 순서의 일관성 보장.
        LinkedHashMap<String, IdNamePublicDto> byId = new LinkedHashMap<>();
        for (Asset asset : fetched) {
            byId.put(asset.getId().getValue(), new IdNamePublicDto(asset.getId().getValue(), asset.getName()));
        }

        List<IdNamePublicDto> result = new ArrayList<>();
        for (String normalizedId : normalizedIds) {
            IdNamePublicDto row = byId.get(normalizedId);
            if (row != null) {
                result.add(row);
            }
        }
        return result;
    }

    /**
     * 소유자의 전체 활성 자산 DTO 목록을 반환한다.
     *
     * @param ownerUserId 소유 사용자 ID
     * @return 활성 자산 DTO 목록
     */
    @Override
    public List<IdNamePublicDto> getAssetsByOwner(String ownerUserId) {
        if (ownerUserId == null || ownerUserId.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<Asset> fetched = assetRepository.findNamesByOwner(ownerUserId);
        List<IdNamePublicDto> result = new ArrayList<>(fetched.size());
        for (Asset asset : fetched) {
            result.add(new IdNamePublicDto(asset.getId().getValue(), asset.getName()));
        }
        return result;
    }
}
