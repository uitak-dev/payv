package com.payv.budget.infrastructure.adapter;

import com.payv.budget.application.port.ClassificationQueryPort;
import com.payv.budget.application.port.ClassificationValidationPort;
import com.payv.budget.application.port.dto.CategoryChildOptionDto;
import com.payv.budget.application.port.dto.CategoryTreeOptionDto;
import com.payv.classification.application.query.CategoryQueryService;
import com.payv.classification.application.query.model.CategoryChildView;
import com.payv.classification.application.query.model.CategoryTreeView;
import com.payv.classification.domain.model.CategoryId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component("budgetClassificationAclAdapter")
@RequiredArgsConstructor
public class InProcessClassificationAclAdapter implements ClassificationValidationPort, ClassificationQueryPort {

    private final CategoryQueryService categoryQueryService;

    @Override
    public void validateCategorization(Collection<String> categoryIds, String ownerUserId) {
        if (categoryIds == null || categoryIds.isEmpty()) return;

        Set<String> rootIds = getAllCategories(ownerUserId).stream()
                .map(CategoryTreeOptionDto::getCategoryId)
                .collect(Collectors.toCollection(HashSet::new));

        for (String categoryId : categoryIds) {
            if (categoryId == null || categoryId.trim().isEmpty()) {
                throw new IllegalArgumentException("categoryId must not be blank");
            }
            if (!rootIds.contains(categoryId)) {
                throw new IllegalStateException("budget category must be active 1-depth category");
            }
        }
        // category type(EXPENSE/INCOME)는 classification 모델에 현재 분리 저장되지 않아,
        // 현 구현에서는 활성 root category를 EXPENSE budget 대상으로 취급한다.
    }

    @Override
    public Map<String, String> getCategoryNames(Collection<String> categoryIds, String ownerUserId) {
        if (categoryIds == null || categoryIds.isEmpty()) return Collections.emptyMap();

        Set<CategoryId> ids = categoryIds.stream()
                .filter(id -> id != null && !id.trim().isEmpty())
                .map(CategoryId::of)
                .collect(Collectors.toCollection(HashSet::new));
        if (ids.isEmpty()) return Collections.emptyMap();

        Map<CategoryId, String> fetched = categoryQueryService.getNamesByIds(ownerUserId, ids);
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<CategoryId, String> entry : fetched.entrySet()) {
            result.put(entry.getKey().getValue(), entry.getValue());
        }
        return result;
    }

    @Override
    public List<CategoryTreeOptionDto> getAllCategories(String ownerUserId) {
        return categoryQueryService.getAll(ownerUserId).stream()
                .map(this::toCategoryTreeOption)
                .collect(Collectors.toList());
    }

    private CategoryTreeOptionDto toCategoryTreeOption(CategoryTreeView root) {
        List<CategoryChildOptionDto> children = root.getChildren() == null
                ? Collections.emptyList()
                : root.getChildren().stream()
                .map(this::toCategoryChildOption)
                .collect(Collectors.toList());

        return new CategoryTreeOptionDto(root.getCategoryId(), root.getName(), children);
    }

    private CategoryChildOptionDto toCategoryChildOption(CategoryChildView child) {
        return new CategoryChildOptionDto(child.getCategoryId(), child.getName());
    }
}
