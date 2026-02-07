package com.payv.ledger.infrastructure.adapter;

import com.payv.classification.application.query.CategoryQueryService;
import com.payv.classification.application.query.TagQueryService;
import com.payv.classification.application.query.model.CategoryChildView;
import com.payv.classification.application.query.model.CategoryTreeView;
import com.payv.classification.domain.model.CategoryId;
import com.payv.classification.domain.model.TagId;
import com.payv.ledger.application.port.ClassificationQueryPort;
import com.payv.ledger.application.port.ClassificationValidationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Collection;
import java.util.Iterator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class InProcessClassificationAclAdapter implements ClassificationValidationPort, ClassificationQueryPort {

    private final CategoryQueryService categoryQueryService;
    private final TagQueryService tagQueryService;

    @Override
    public void validateTagIds(Collection<String> tagIds, String ownerUserId) {
        if (tagIds == null || tagIds.isEmpty()) return;

        Set<String> normalized = new LinkedHashSet<>();
        for (String tagId : tagIds) {
            if (tagId == null || tagId.trim().isEmpty()) {
                throw new IllegalArgumentException("tagId must not be blank");
            }
            normalized.add(tagId);
        }

        Map<String, String> nameMap = getTagNames(normalized, ownerUserId);
        if (nameMap.size() != normalized.size()) {
            throw new IllegalStateException("invalid or inactive tag included");
        }
    }

    @Override
    public void validateCategorization(Collection<String> categoryIds, String ownerUserId) {
        if (categoryIds == null || categoryIds.isEmpty()) return;

        Set<String> normalized = new LinkedHashSet<>();
        for (String categoryId : categoryIds) {
            if (categoryId == null || categoryId.trim().isEmpty()) {
                throw new IllegalArgumentException("categoryId must not be blank");
            }
            normalized.add(categoryId);
        }

        Map<String, String> nameMap = getCategoryNames(normalized, ownerUserId);
        if (nameMap.size() != normalized.size()) {
            throw new IllegalStateException("invalid or inactive category included");
        }
    }

    @Override
    public Map<String, String> getTagNames(Collection<String> tagIds, String ownerUserId) {
        if (tagIds == null || tagIds.isEmpty()) return Collections.emptyMap();

        Set<TagId> ids = tagIds.stream()
                .filter(id -> id != null && !id.trim().isEmpty())
                .map(TagId::of)
                .collect(Collectors.toCollection(HashSet::new));
        if (ids.isEmpty()) return Collections.emptyMap();

        Map<TagId, String> fetched = tagQueryService.getNamesByIds(ownerUserId, ids);
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<TagId, String> entry : fetched.entrySet()) {
            result.put(entry.getKey().getValue(), entry.getValue());
        }
        return result;
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
}
