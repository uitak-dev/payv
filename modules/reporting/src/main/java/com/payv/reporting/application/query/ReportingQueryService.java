package com.payv.reporting.application.query;

import com.payv.reporting.application.port.AssetLookupPort;
import com.payv.reporting.application.port.BudgetSnapshotPort;
import com.payv.reporting.application.port.ClassificationLookupPort;
import com.payv.reporting.application.port.LedgerReportQueryPort;
import com.payv.reporting.application.port.dto.AmountByIdDto;
import com.payv.reporting.application.port.dto.OverallBudgetSnapshotDto;
import com.payv.reporting.application.port.dto.RecentTransactionDto;
import com.payv.reporting.application.query.model.*;
import com.payv.reporting.domain.model.MonthlyReport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 리포팅 화면에서 필요한 월간 집계/대시보드 조회를 조합하는 애플리케이션 서비스.
 * 외부 BC 데이터 접근은 포트(ACL)를 통해 수행한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportingQueryService {

    private static final int HOME_RECENT_LIMIT = 20;

    private final LedgerReportQueryPort ledgerReportQueryPort;
    private final BudgetSnapshotPort budgetSnapshotPort;
    private final AssetLookupPort assetLookupPort;
    private final ClassificationLookupPort classificationLookupPort;

    /**
     * 리포트 화면용 월간 집계 뷰 모델을 생성한다.
     *
     * @param ownerUserId 소유 사용자 ID
     * @param month 조회 대상 월. {@code null}이면 현재 월
     * @return 월간 리포트 뷰
     */
    public MonthlyReportView getMonthlyReport(String ownerUserId, YearMonth month) {
        YearMonth targetMonth = month == null ? YearMonth.now() : month;
        LocalDate from = targetMonth.atDay(1);
        LocalDate to = targetMonth.atEndOfMonth();

        long expense = ledgerReportQueryPort.sumAmountByType(ownerUserId, from, to, "EXPENSE");
        long income = ledgerReportQueryPort.sumAmountByType(ownerUserId, from, to, "INCOME");

        Optional<OverallBudgetSnapshotDto> overallBudget =
                budgetSnapshotPort.findOverallBudget(ownerUserId, targetMonth);

        MonthlyReport aggregate = MonthlyReport.of(
                expense,
                income,
                overallBudget.map(OverallBudgetSnapshotDto::getAmountLimit).orElse(0L)
        );

        List<AmountByIdDto> assetRows = ledgerReportQueryPort.sumExpenseByAsset(ownerUserId, from, to);
        List<AmountByIdDto> categoryRows = ledgerReportQueryPort.sumExpenseByCategoryLevel1(ownerUserId, from, to);
        List<AmountByIdDto> tagRows = ledgerReportQueryPort.sumExpenseByTag(ownerUserId, from, to);

        Map<String, String> assetNames = assetLookupPort.getAssetNames(extractIds(assetRows), ownerUserId);
        Map<String, String> categoryNames = classificationLookupPort.getCategoryNames(extractIds(categoryRows), ownerUserId);
        Map<String, String> tagNames = classificationLookupPort.getTagNames(extractIds(tagRows), ownerUserId);

        return new MonthlyReportView(
                targetMonth,
                expense,
                income,
                aggregate.netAmount(),
                overallBudget.map(OverallBudgetSnapshotDto::getAmountLimit).orElse(0L),
                overallBudget.map(OverallBudgetSnapshotDto::getSpentAmount).orElse(expense),
                overallBudget.map(OverallBudgetSnapshotDto::getRemainingAmount).orElse(0L),
                aggregate.budgetUsageRate(),
                toBreakdowns(assetRows, assetNames, aggregate),
                toBreakdowns(categoryRows, categoryNames, aggregate),
                toTagSummaries(tagRows, tagNames)
        );
    }

    /**
     * 홈 화면용 대시보드 요약 뷰 모델을 생성한다.
     *
     * @param ownerUserId 소유 사용자 ID
     * @param month 조회 대상 월. {@code null}이면 현재 월
     * @return 홈 대시보드 뷰
     */
    public HomeDashboardView getHomeDashboard(String ownerUserId, YearMonth month) {
        YearMonth targetMonth = month == null ? YearMonth.now() : month;
        LocalDate from = targetMonth.atDay(1);
        LocalDate to = targetMonth.atEndOfMonth();

        long expense = ledgerReportQueryPort.sumAmountByType(ownerUserId, from, to, "EXPENSE");
        long income = ledgerReportQueryPort.sumAmountByType(ownerUserId, from, to, "INCOME");

        Optional<OverallBudgetSnapshotDto> overallBudget =
                budgetSnapshotPort.findOverallBudget(ownerUserId, targetMonth);

        List<RecentTransactionDto> recentRows =
                ledgerReportQueryPort.findRecentTransactions(ownerUserId, from, to, HOME_RECENT_LIMIT);

        Set<String> assetIds = new LinkedHashSet<>();
        Set<String> categoryIds = new LinkedHashSet<>();
        for (RecentTransactionDto recent : recentRows) {
            if (recent.getAssetId() != null && !recent.getAssetId().trim().isEmpty()) {
                assetIds.add(recent.getAssetId());
            }
            if (recent.getCategoryIdLevel1() != null && !recent.getCategoryIdLevel1().trim().isEmpty()) {
                categoryIds.add(recent.getCategoryIdLevel1());
            }
        }

        Map<String, String> assetNames = assetLookupPort.getAssetNames(assetIds, ownerUserId);
        Map<String, String> categoryNames = classificationLookupPort.getCategoryNames(categoryIds, ownerUserId);

        List<RecentTransactionView> recentTransactions = new ArrayList<>(recentRows.size());
        for (RecentTransactionDto row : recentRows) {
            recentTransactions.add(new RecentTransactionView(
                    row.getTransactionId(),
                    row.getTransactionType(),
                    row.getAmount(),
                    row.getTransactionDate(),
                    row.getAssetId(),
                    assetNames.get(row.getAssetId()),
                    row.getCategoryIdLevel1(),
                    categoryNames.get(row.getCategoryIdLevel1()),
                    row.getMemo()
            ));
        }

        return new HomeDashboardView(
                targetMonth,
                from,
                to,
                income,
                expense,
                income - expense,
                overallBudget.isPresent(),
                overallBudget.map(OverallBudgetSnapshotDto::getRemainingAmount).orElse(0L),
                overallBudget.map(OverallBudgetSnapshotDto::getUsageRate).orElse(0),
                recentTransactions
        );
    }

    private List<BreakdownView> toBreakdowns(List<AmountByIdDto> rows,
                                             Map<String, String> names,
                                             MonthlyReport aggregate) {
        if (rows == null || rows.isEmpty()) return Collections.emptyList();

        List<BreakdownView> ret = new ArrayList<>(rows.size());
        for (AmountByIdDto row : rows) {
            String refId = row.getRefId();
            String name = names.get(refId);
            ret.add(new BreakdownView(
                    refId,
                    (name == null || name.trim().isEmpty()) ? "(이름 없음)" : name,
                    row.getAmount(),
                    aggregate.percentageOfExpense(row.getAmount())
            ));
        }
        return ret;
    }

    private List<TagSummaryView> toTagSummaries(List<AmountByIdDto> rows,
                                                Map<String, String> names) {
        if (rows == null || rows.isEmpty()) return Collections.emptyList();

        List<TagSummaryView> ret = new ArrayList<>(rows.size());
        for (AmountByIdDto row : rows) {
            String refId = row.getRefId();
            String name = names.get(refId);
            ret.add(new TagSummaryView(
                    refId,
                    (name == null || name.trim().isEmpty()) ? "(이름 없음)" : name,
                    row.getAmount()
            ));
        }
        return ret;
    }

    private Collection<String> extractIds(List<AmountByIdDto> rows) {
        if (rows == null || rows.isEmpty()) return Collections.emptySet();

        Set<String> ids = new LinkedHashSet<>();
        for (AmountByIdDto row : rows) {
            if (row.getRefId() == null || row.getRefId().trim().isEmpty()) continue;
            ids.add(row.getRefId());
        }
        return ids;
    }

}
