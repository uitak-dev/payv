package com.payv.reporting.application.port;

import com.payv.reporting.application.port.dto.OverallBudgetSnapshotDto;

import java.time.YearMonth;
import java.util.Optional;

/**
 * 리포팅 BC가 예산 BC의 월별 전체 예산 스냅샷을 조회하기 위한 ACL 포트.
 */
public interface BudgetSnapshotPort {

    /**
     * 대상 월의 전체 예산(카테고리 미지정 예산)을 조회한다.
     *
     * @param ownerUserId 소유 사용자 ID
     * @param targetMonth 조회 대상 월
     * @return 전체 예산 스냅샷. 없으면 {@link Optional#empty()}
     */
    Optional<OverallBudgetSnapshotDto> findOverallBudget(String ownerUserId, YearMonth targetMonth);
}
