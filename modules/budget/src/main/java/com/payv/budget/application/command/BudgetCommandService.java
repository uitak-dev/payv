package com.payv.budget.application.command;

import com.payv.budget.application.command.model.CreateBudgetCommand;
import com.payv.budget.application.command.model.DeactivateBudgetCommand;
import com.payv.budget.application.command.model.UpdateBudgetCommand;
import com.payv.budget.application.exception.BudgetNotFoundException;
import com.payv.budget.application.exception.DuplicateBudgetException;
import com.payv.budget.application.port.ClassificationQueryPort;
import com.payv.budget.application.port.ClassificationValidationPort;
import com.payv.common.cache.CacheNames;
import com.payv.common.error.InvalidRequestException;
import com.payv.budget.domain.model.Budget;
import com.payv.budget.domain.model.BudgetId;
import com.payv.budget.domain.repository.BudgetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
/**
 * 예산 변경 명령을 처리하는 서비스.
 * - 예산 생성/수정/비활성화와 고아 예산 정리(카테고리 삭제 전파)를 수행한다.
 * - 월/카테고리 단위 중복 방지, 카테고리 유효성 검증 등 예산 정책을 저장 전에 강제한다.
 */
public class BudgetCommandService {

    private final BudgetRepository budgetRepository;

    private final ClassificationValidationPort classificationValidationPort;
    private final ClassificationQueryPort classificationQueryPort;

    /**
     * 예산을 생성한다.
     *
     * @param command 생성 요청(대상 월, 한도, 카테고리, 메모)
     * @param ownerUserId 소유 사용자 ID
     * @return 생성된 예산 ID
     * @throws NullPointerException {@code command}가 {@code null}인 경우
     * @throws InvalidRequestException {@code ownerUserId}가 비어 있는 경우
     * @throws DuplicateBudgetException 동일 월/동일 카테고리(또는 전체) 예산이 이미 존재하는 경우
     */
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.BUDGET_MONTHLY_STATUS, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.REPORTING_MONTHLY_SUMMARY, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.REPORTING_HOME_DASHBOARD, allEntries = true)
    })
    public BudgetId create(CreateBudgetCommand command, String ownerUserId) {
        Objects.requireNonNull(command, "command");
        requireOwner(ownerUserId);
        cleanupOrphanedCategoryBudgets(ownerUserId);

        validateCategoryIfPresent(command.getCategoryId(), ownerUserId);
        ensureUniqueBudget(ownerUserId, command.getTargetMonth(), command.getCategoryId(), null);

        Budget budget = Budget.create(
                ownerUserId,
                command.getTargetMonth(),
                command.getAmountLimit(),
                command.getCategoryId(),
                command.getMemo()
        );

        budgetRepository.save(budget, ownerUserId);
        return budget.getId();
    }

    /**
     * 예산을 수정한다.
     *
     * @param command 수정 요청(예산 ID, 대상 월, 한도, 카테고리, 메모)
     * @param ownerUserId 소유 사용자 ID
     * @throws NullPointerException {@code command}가 {@code null}인 경우
     * @throws InvalidRequestException {@code ownerUserId}가 비어 있는 경우
     * @throws BudgetNotFoundException 대상 예산이 없거나 소유자가 다른 경우
     * @throws DuplicateBudgetException 수정 결과가 중복 예산 조건을 만족하는 경우
     */
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.BUDGET_MONTHLY_STATUS, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.REPORTING_MONTHLY_SUMMARY, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.REPORTING_HOME_DASHBOARD, allEntries = true)
    })
    public void update(UpdateBudgetCommand command, String ownerUserId) {
        Objects.requireNonNull(command, "command");
        requireOwner(ownerUserId);
        cleanupOrphanedCategoryBudgets(ownerUserId);

        Budget budget = budgetRepository.findById(command.getBudgetId(), ownerUserId)
                .orElseThrow(BudgetNotFoundException::new);

        validateCategoryIfPresent(command.getCategoryId(), ownerUserId);
        ensureUniqueBudget(ownerUserId, command.getTargetMonth(), command.getCategoryId(), budget.getId());

        budget.update(
                command.getTargetMonth(),
                command.getAmountLimit(),
                command.getCategoryId(),
                command.getMemo()
        );

        budgetRepository.save(budget, ownerUserId);
    }

    /**
     * 예산을 비활성화한다(소프트 삭제).
     *
     * @param command 비활성화 요청(예산 ID)
     * @param ownerUserId 소유 사용자 ID
     * @throws NullPointerException {@code command}가 {@code null}인 경우
     * @throws InvalidRequestException {@code ownerUserId}가 비어 있는 경우
     * @throws BudgetNotFoundException 대상 예산이 없거나 소유자가 다른 경우
     */
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.BUDGET_MONTHLY_STATUS, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.REPORTING_MONTHLY_SUMMARY, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.REPORTING_HOME_DASHBOARD, allEntries = true)
    })
    public void deactivate(DeactivateBudgetCommand command, String ownerUserId) {
        Objects.requireNonNull(command, "command");
        requireOwner(ownerUserId);
        cleanupOrphanedCategoryBudgets(ownerUserId);

        Budget budget = budgetRepository.findById(command.getBudgetId(), ownerUserId)
                .orElseThrow(BudgetNotFoundException::new);

        budget.deactivate();
        budgetRepository.save(budget, ownerUserId);
    }

    /**
     * 분류 BC 상태를 기준으로 고아 카테고리 예산을 비활성화한다.
     * - 카테고리 삭제 전파 정책(정합성 가드레일)을 예산 BC에서 반영한다.
     *
     * @param ownerUserId 소유 사용자 ID
     * @throws InvalidRequestException {@code ownerUserId}가 비어 있는 경우
     */
    public void cleanupOrphanedCategoryBudgets(String ownerUserId) {
        requireOwner(ownerUserId);

        Set<String> activeRootCategoryIds = classificationQueryPort.getAllCategories(ownerUserId).stream()
                .map(c -> c.getCategoryId())
                .collect(Collectors.toCollection(HashSet::new));
        budgetRepository.deactivateOrphanedCategoryBudgets(ownerUserId, activeRootCategoryIds);
    }

    private void validateCategoryIfPresent(String categoryId, String ownerUserId) {
        if (categoryId == null || categoryId.trim().isEmpty()) return;
        classificationValidationPort.validateCategorization(Collections.singleton(categoryId), ownerUserId);
    }

    private void ensureUniqueBudget(String ownerUserId, YearMonth targetMonth,
                                    String categoryId, BudgetId excludeId) {
        List<Budget> budgets = budgetRepository.findAllByOwnerAndMonth(ownerUserId, targetMonth);
        for (Budget budget : budgets) {
            if (excludeId != null && budget.getId().equals(excludeId)) {
                continue;
            }

            if (Objects.equals(budget.getCategoryId(), normalizeNullable(categoryId))) {
                throw new DuplicateBudgetException();
            }
        }
    }

    private String normalizeNullable(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static void requireOwner(String ownerUserId) {
        if (ownerUserId == null || ownerUserId.trim().isEmpty()) {
            throw new InvalidRequestException("ownerUserId must not be blank");
        }
    }
}
