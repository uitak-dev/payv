package com.payv.ledger.infrastructure.adapter;

import com.payv.contracts.classification.ClassificationPublicApi;
import com.payv.contracts.classification.dto.CategoryTreePublicDto;
import com.payv.contracts.common.dto.IdNamePublicDto;
import com.payv.common.error.InvalidRequestException;
import com.payv.ledger.application.exception.InvalidLedgerReferenceException;
import com.payv.ledger.application.port.ClassificationQueryPort;
import com.payv.ledger.application.port.ClassificationValidationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component("ledgerClassificationAclAdapter")
@RequiredArgsConstructor
public class InProcessClassificationAclAdapter implements ClassificationValidationPort, ClassificationQueryPort {

    private final ClassificationPublicApi classificationPublicService;

    @Override
    public void validateTagIds(Collection<String> tagIds, String ownerUserId) {
        if (tagIds == null || tagIds.isEmpty()) return;

        Set<String> normalized = new LinkedHashSet<>();
        for (String tagId : tagIds) {
            if (tagId == null || tagId.trim().isEmpty()) {
                throw new InvalidRequestException("tagId must not be blank");
            }
            normalized.add(tagId);
        }

        List<IdNamePublicDto> tags = getTagNames(normalized, ownerUserId);
        if (tags.size() != normalized.size()) {
            throw new InvalidLedgerReferenceException("invalid or inactive tag included");
        }
    }

    @Override
    public void validateCategorization(Collection<String> categoryIds, String ownerUserId) {
        if (categoryIds == null || categoryIds.isEmpty()) return;

        Set<String> normalized = new LinkedHashSet<>();
        for (String categoryId : categoryIds) {
            if (categoryId == null || categoryId.trim().isEmpty()) {
                throw new InvalidRequestException("categoryId must not be blank");
            }
            normalized.add(categoryId);
        }

        List<IdNamePublicDto> categories = getCategoryNames(normalized, ownerUserId);
        if (categories.size() != normalized.size()) {
            throw new InvalidLedgerReferenceException("invalid or inactive category included");
        }
    }

    @Override
    public List<IdNamePublicDto> getTagNames(Collection<String> tagIds, String ownerUserId) {
        if (tagIds == null || tagIds.isEmpty()) return Collections.emptyList();
        return classificationPublicService.getTagsByIds(ownerUserId, tagIds);
    }

    @Override
    public List<IdNamePublicDto> getCategoryNames(Collection<String> categoryIds, String ownerUserId) {
        if (categoryIds == null || categoryIds.isEmpty()) return Collections.emptyList();
        return classificationPublicService.getCategoriesByIds(ownerUserId, categoryIds);
    }

    @Override
    public List<IdNamePublicDto> getAllTags(String ownerUserId) {
        return classificationPublicService.getAllTags(ownerUserId);
    }

    @Override
    public List<CategoryTreePublicDto> getAllCategories(String ownerUserId) {
        return classificationPublicService.getAllCategoryTrees(ownerUserId);
    }
}
