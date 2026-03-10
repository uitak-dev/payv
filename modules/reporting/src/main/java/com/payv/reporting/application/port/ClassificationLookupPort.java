package com.payv.reporting.application.port;

import com.payv.contracts.common.dto.IdNamePublicDto;

import java.util.Collection;
import java.util.List;

/**
 * 리포팅 BC가 분류(Classification) BC의 카테고리/태그 이름을 조회하기 위한 ACL 포트.
 */
public interface ClassificationLookupPort {

    /**
     * 카테고리 ID 목록의 이름 매핑을 조회한다.
     *
     * @param categoryIds 조회 대상 카테고리 ID 목록
     * @param ownerUserId 소유 사용자 ID
     * @return 카테고리 ID/이름 목록
     */
    List<IdNamePublicDto> getCategoryNames(Collection<String> categoryIds, String ownerUserId);

    /**
     * 태그 ID 목록의 이름 매핑을 조회한다.
     *
     * @param tagIds 조회 대상 태그 ID 목록
     * @param ownerUserId 소유 사용자 ID
     * @return 태그 ID/이름 목록
     */
    List<IdNamePublicDto> getTagNames(Collection<String> tagIds, String ownerUserId);
}
