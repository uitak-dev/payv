package com.payv.automation.infrastructure.adapter;

import com.payv.automation.application.exception.InvalidFixedExpenseReferenceException;
import com.payv.automation.application.port.ClassificationQueryPort;
import com.payv.automation.application.port.ClassificationValidationPort;
import com.payv.contracts.classification.ClassificationPublicApi;
import com.payv.contracts.classification.dto.CategoryTreePublicDto;
import com.payv.contracts.common.dto.IdNamePublicDto;
import com.payv.common.error.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("automationClassificationAclAdapter")
@RequiredArgsConstructor
public class InProcessClassificationAclAdapter implements ClassificationValidationPort, ClassificationQueryPort {

    private final ClassificationPublicApi classificationPublicService;

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

        List<IdNamePublicDto> categories = getCategoryNames(normalized, ownerUserId);
        if (categories.size() != normalized.size()) {
            throw new InvalidFixedExpenseReferenceException("invalid or inactive category included");
        }
    }

    @Override
    public List<IdNamePublicDto> getCategoryNames(Collection<String> categoryIds, String ownerUserId) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return Collections.emptyList();
        }
        return classificationPublicService.getCategoriesByIds(ownerUserId, categoryIds);
    }

    @Override
    public List<CategoryTreePublicDto> getAllCategories(String ownerUserId) {
        return classificationPublicService.getAllCategoryTrees(ownerUserId);
    }
}
