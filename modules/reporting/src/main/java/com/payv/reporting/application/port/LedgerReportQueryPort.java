package com.payv.reporting.application.port;

import com.payv.reporting.application.port.dto.AmountByIdDto;
import com.payv.reporting.application.port.dto.RecentTransactionDto;

import java.time.LocalDate;
import java.util.List;

/**
 * 리포팅 BC가 원장(Ledger) BC의 집계/조회 데이터를 가져오기 위한 ACL 포트.
 */
public interface LedgerReportQueryPort {

    /**
     * 거래 유형(INCOME/EXPENSE) 기준 합계 금액을 조회한다.
     *
     * @param ownerUserId 소유 사용자 ID
     * @param from 시작 거래일(포함)
     * @param to 종료 거래일(포함)
     * @param transactionType 거래 유형
     * @return 합계 금액
     */
    long sumAmountByType(String ownerUserId, LocalDate from, LocalDate to, String transactionType);

    /**
     * 자산별 지출 합계 목록을 조회한다.
     */
    List<AmountByIdDto> sumExpenseByAsset(String ownerUserId, LocalDate from, LocalDate to);

    /**
     * 1단계 카테고리별 지출 합계 목록을 조회한다.
     */
    List<AmountByIdDto> sumExpenseByCategoryLevel1(String ownerUserId, LocalDate from, LocalDate to);

    /**
     * 태그별 지출 합계 목록을 조회한다.
     */
    List<AmountByIdDto> sumExpenseByTag(String ownerUserId, LocalDate from, LocalDate to);

    /**
     * 홈 대시보드 표시에 필요한 최근 거래 목록을 조회한다.
     *
     * @param limit 최대 건수
     */
    List<RecentTransactionDto> findRecentTransactions(String ownerUserId, LocalDate from, LocalDate to, int limit);
}
