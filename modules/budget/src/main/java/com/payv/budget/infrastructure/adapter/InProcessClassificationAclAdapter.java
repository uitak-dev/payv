package com.payv.budget.infrastructure.adapter;

import com.payv.budget.application.exception.InvalidBudgetCategoryException;
import com.payv.budget.application.port.ClassificationQueryPort;
import com.payv.budget.application.port.ClassificationValidationPort;
import com.payv.contracts.classification.ClassificationPublicApi;
import com.payv.contracts.classification.dto.CategoryTreePublicDto;
import com.payv.contracts.common.dto.IdNamePublicDto;
import com.payv.common.error.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component("budgetClassificationAclAdapter")
@RequiredArgsConstructor
public class InProcessClassificationAclAdapter implements ClassificationValidationPort, ClassificationQueryPort {

    private final ClassificationPublicApi classificationPublicService;

    @Override
    public void validateCategorization(Collection<String> categoryIds, String ownerUserId) {
        if (categoryIds == null || categoryIds.isEmpty()) return;

        Set<String> rootIds = getAllCategories(ownerUserId).stream()
                .map(CategoryTreePublicDto::getCategoryId)
                .collect(Collectors.toCollection(HashSet::new));

        for (String categoryId : categoryIds) {
            if (categoryId == null || categoryId.trim().isEmpty()) {
                throw new InvalidRequestException("categoryId must not be blank");
            }
            if (!rootIds.contains(categoryId)) {
                throw new InvalidBudgetCategoryException();
            }
        }
        // category type(EXPENSE/INCOME)는 classification 모델에 현재 분리 저장되지 않아,
        // 현 구현에서는 활성 root category를 EXPENSE budget 대상으로 취급한다.
    }

    @Override
    public List<IdNamePublicDto> getCategoryNames(Collection<String> categoryIds, String ownerUserId) {
        if (categoryIds == null || categoryIds.isEmpty()) return Collections.emptyList();
        return classificationPublicService.getCategoriesByIds(ownerUserId, categoryIds);
    }

    @Override
    public List<CategoryTreePublicDto> getAllCategories(String ownerUserId) {
        return classificationPublicService.getAllCategoryTrees(ownerUserId);
    }
}
