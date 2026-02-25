package com.payv.automation.infrastructure.adapter;

import com.payv.automation.application.exception.InvalidFixedExpenseReferenceException;
import com.payv.automation.application.port.ClassificationQueryPort;
import com.payv.automation.application.port.ClassificationValidationPort;
import com.payv.automation.application.port.dto.CategoryChildOptionDto;
import com.payv.automation.application.port.dto.CategoryTreeOptionDto;
import com.payv.classification.application.query.CategoryQueryService;
import com.payv.classification.application.query.model.CategoryChildView;
import com.payv.classification.application.query.model.CategoryTreeView;
import com.payv.classification.domain.model.CategoryId;
import com.payv.common.error.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component("automationClassificationAclAdapter")
@RequiredArgsConstructor
public class InProcessClassificationAclAdapter implements ClassificationValidationPort, ClassificationQueryPort {

    private final CategoryQueryService categoryQueryService;

    @Override
    public void validateCategorization(Collection<String> categoryIds, String ownerUserId) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return;
        }

        Set<String> normalized = new LinkedHashSet<>();
        for (String categoryId : categoryIds) {
            if (categoryId == null || categoryId.trim().isEmpty()) {
                throw new InvalidRequestException("categoryId must not be blank");
            }
            normalized.add(categoryId.trim());
        }

        Map<String, String> categoryNames = getCategoryNames(normalized, ownerUserId);
        if (categoryNames.size() != normalized.size()) {
            throw new InvalidFixedExpenseReferenceException("invalid or inactive category included");
        }
    }

    @Override
    public Map<String, String> getCategoryNames(Collection<String> categoryIds, String ownerUserId) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<CategoryId> ids = new LinkedHashSet<>();
        for (String categoryId : categoryIds) {
            if (categoryId == null || categoryId.trim().isEmpty()) {
                continue;
            }
            ids.add(CategoryId.of(categoryId.trim()));
        }
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }

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
