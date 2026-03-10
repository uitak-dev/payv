package com.payv.reporting.application.port;

import com.payv.contracts.common.dto.IdNamePublicDto;

import java.util.Collection;
import java.util.List;

/**
 * 리포팅 BC가 자산 BC로부터 자산 식별자에 대응하는 표시명을 조회하기 위한 ACL 포트.
 */
public interface AssetLookupPort {

    /**
     * 자산 ID 목록에 대한 자산명 매핑을 조회한다.
     *
     * @param assetIds 조회 대상 자산 ID 목록
     * @param ownerUserId 소유 사용자 ID
     * @return 자산 ID/이름 목록
     */
    List<IdNamePublicDto> getAssetNames(Collection<String> assetIds, String ownerUserId);
}
