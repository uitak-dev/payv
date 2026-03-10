package com.payv.budget.application.query;

import com.payv.budget.application.port.ClassificationQueryPort;
import com.payv.budget.application.port.LedgerSpendingQueryPort;
import com.payv.budget.application.query.model.BudgetView;
import com.payv.common.cache.CacheNames;
import com.payv.contracts.common.dto.IdNamePublicDto;
import com.payv.budget.domain.model.Budget;
import com.payv.budget.domain.model.BudgetId;
import com.payv.budget.domain.repository.BudgetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
/**
 * 예산 조회 서비스.
 * - 월별 예산 목록과 단건 예산의 소진 현황(사용액/잔여/사용률)을 계산한다.
 * - 소진액은 저장 값이 아닌 Ledger 집계를 기준으로 조회 시점에 계산하여,
 *   최신 거래 데이터와 정합성을 맞춘다.
 */
public class BudgetQueryService {

    private final BudgetRepository budgetRepository;

    private final ClassificationQueryPort classificationQueryPort;
    private final LedgerSpendingQueryPort ledgerSpendingQueryPort;

    /**
     * 월별 예산 목록과 소진 현황을 조회한다.
     *
     * @param ownerUserId 소유 사용자 ID
     * @param yearMonth 대상 월({@code null}이면 현재 월)
     * @return 월별 예산 뷰 목록(카테고리명/소진액/잔여액/사용률 포함)
     */
    @Cacheable(
            cacheNames = CacheNames.BUDGET_MONTHLY_STATUS,
            key = "T(com.payv.common.cache.CacheKeys).budgetMonthlyStatusKey(#ownerUserId, #yearMonth)"
    )
    public List<BudgetView> getMonthlyBudgets(String ownerUserId, YearMonth yearMonth) {
        YearMonth targetMonth = yearMonth == null ? YearMonth.now() : yearMonth;
        LocalDate monthStart = targetMonth.atDay(1);
        LocalDate monthEnd = targetMonth.atEndOfMonth();

        List<Budget> budgets = budgetRepository.findAllByOwnerAndMonth(ownerUserId, targetMonth).stream()
                .filter(Budget::isActive)
                .collect(Collectors.toList());

        Set<String> categoryIds = budgets.stream()
                .map(Budget::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<String, String> categoryNames = categoryIds.isEmpty()
                ? Collections.emptyMap()
                : toNameMap(classificationQueryPort.getCategoryNames(categoryIds, ownerUserId));

        List<BudgetView> ret = new ArrayList<>(budgets.size());
        for (Budget budget : budgets) {
            long spent = ledgerSpendingQueryPort.sumExpenseAmount(
                    ownerUserId,
                    monthStart,
                    monthEnd,
                    budget.getCategoryId()
            );

            long remaining = budget.getAmountLimit() - spent;
            int usageRate = (int) ((spent * 100.0d) / budget.getAmountLimit());

            ret.add(new BudgetView(
                    budget.getId().getValue(),
                    budget.getTargetMonth().toString(),
                    budget.getCategoryId(),
                    budget.isOverallBudget() ? "전체 예산" : categoryNames.get(budget.getCategoryId()),
                    budget.getAmountLimit(),
                    budget.getMemo(),
                    spent,
                    remaining,
                    usageRate
            ));
        }

        ret.sort(Comparator.comparing(BudgetView::getCategoryName, Comparator.nullsLast(String::compareTo)));
        return ret;
    }

    /**
     * 예산 단건과 해당 월의 소진 현황을 조회한다.
     *
     * @param budgetId 예산 ID
     * @param ownerUserId 소유 사용자 ID
     * @return 예산 뷰. 없으면 {@link Optional#empty()}
     */
    public Optional<BudgetView> get(BudgetId budgetId, String ownerUserId) {
        return budgetRepository.findById(budgetId, ownerUserId)
                .map(budget -> {
                    YearMonth targetMonth = budget.getTargetMonth();
                    LocalDate monthStart = targetMonth.atDay(1);
                    LocalDate monthEnd = targetMonth.atEndOfMonth();

                    String categoryId = budget.getCategoryId();
                    Map<String, String> categoryNames = categoryId == null
                            ? Collections.emptyMap()
                            : toNameMap(classificationQueryPort.getCategoryNames(Collections.singleton(categoryId), ownerUserId));

                    long spent = ledgerSpendingQueryPort.sumExpenseAmount(
                            ownerUserId,
                            monthStart,
                            monthEnd,
                            categoryId
                    );
                    long remaining = budget.getAmountLimit() - spent;
                    int usageRate = (int) ((spent * 100.0d) / budget.getAmountLimit());

                    return new BudgetView(
                            budget.getId().getValue(),
                            targetMonth.toString(),
                            categoryId,
                            budget.isOverallBudget() ? "전체 예산" : categoryNames.get(categoryId),
                            budget.getAmountLimit(),
                            budget.getMemo(),
                            spent,
                            remaining,
                            usageRate
                    );
                });
    }

    private Map<String, String> toNameMap(List<IdNamePublicDto> rows) {
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new LinkedHashMap<>();
        for (IdNamePublicDto row : rows) {
            result.put(row.getId(), row.getName());
        }
        return result;
    }
}
