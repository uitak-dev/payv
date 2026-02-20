package com.payv.reporting.infrastructure.adapter;

import com.payv.reporting.application.port.LedgerReportQueryPort;
import com.payv.reporting.application.port.dto.AmountByIdDto;
import com.payv.reporting.application.port.dto.RecentTransactionDto;
import com.payv.reporting.infrastructure.persistence.mybatis.mapper.ReportingLedgerReadMapper;
import com.payv.reporting.infrastructure.persistence.mybatis.record.AmountByIdRecord;
import com.payv.reporting.infrastructure.persistence.mybatis.record.RecentTransactionRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component("reportingLedgerAclAdapter")
@RequiredArgsConstructor
public class InProcessLedgerAclAdapter implements LedgerReportQueryPort {

    private final ReportingLedgerReadMapper reportingLedgerReadMapper;

    @Override
    public long sumAmountByType(String ownerUserId, LocalDate from, LocalDate to, String transactionType) {
        Long value = reportingLedgerReadMapper.sumAmountByType(ownerUserId, from, to, transactionType);
        return value == null ? 0L : value;
    }

    @Override
    public List<AmountByIdDto> sumExpenseByAsset(String ownerUserId, LocalDate from, LocalDate to) {
        return toAmountByIdDtos(reportingLedgerReadMapper.sumExpenseByAsset(ownerUserId, from, to));
    }

    @Override
    public List<AmountByIdDto> sumExpenseByCategoryLevel1(String ownerUserId, LocalDate from, LocalDate to) {
        return toAmountByIdDtos(reportingLedgerReadMapper.sumExpenseByCategoryLevel1(ownerUserId, from, to));
    }

    @Override
    public List<AmountByIdDto> sumExpenseByTag(String ownerUserId, LocalDate from, LocalDate to) {
        return toAmountByIdDtos(reportingLedgerReadMapper.sumExpenseByTag(ownerUserId, from, to));
    }

    @Override
    public List<RecentTransactionDto> findRecentTransactions(String ownerUserId, LocalDate from, LocalDate to, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        List<RecentTransactionRecord> rows =
                reportingLedgerReadMapper.selectRecentTransactions(ownerUserId, from, to, safeLimit);

        if (rows == null || rows.isEmpty()) return Collections.emptyList();

        List<RecentTransactionDto> ret = new ArrayList<>(rows.size());
        for (RecentTransactionRecord row : rows) {
            ret.add(new RecentTransactionDto(
                    row.getTransactionId(),
                    row.getTransactionType(),
                    row.getAmount(),
                    row.getTransactionDate(),
                    row.getAssetId(),
                    row.getCategoryIdLevel1(),
                    row.getMemo()
            ));
        }
        return ret;
    }

    private List<AmountByIdDto> toAmountByIdDtos(List<AmountByIdRecord> rows) {
        if (rows == null || rows.isEmpty()) return Collections.emptyList();

        List<AmountByIdDto> ret = new ArrayList<>(rows.size());
        for (AmountByIdRecord row : rows) {
            ret.add(new AmountByIdDto(row.getRefId(), row.getAmount()));
        }
        return ret;
    }
}
