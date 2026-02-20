package com.payv.reporting.infrastructure.adapter;

import com.payv.classification.application.query.CategoryQueryService;
import com.payv.classification.application.query.TagQueryService;
import com.payv.classification.domain.model.CategoryId;
import com.payv.classification.domain.model.TagId;
import com.payv.reporting.application.port.ClassificationLookupPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Component("reportingClassificationAclAdapter")
@RequiredArgsConstructor
public class InProcessClassificationAclAdapter implements ClassificationLookupPort {

    private final CategoryQueryService categoryQueryService;
    private final TagQueryService tagQueryService;

    @Override
    public Map<String, String> getCategoryNames(Collection<String> categoryIds, String ownerUserId) {
        if (categoryIds == null || categoryIds.isEmpty()) return Collections.emptyMap();

        Set<CategoryId> ids = new LinkedHashSet<>();
        for (String categoryId : categoryIds) {
            if (categoryId == null || categoryId.trim().isEmpty()) continue;
            ids.add(CategoryId.of(categoryId));
        }
        if (ids.isEmpty()) return Collections.emptyMap();

        Map<CategoryId, String> fetched = categoryQueryService.getNamesByIds(ownerUserId, ids);
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<CategoryId, String> entry : fetched.entrySet()) {
            result.put(entry.getKey().getValue(), entry.getValue());
        }
        return result;
    }

    @Override
    public Map<String, String> getTagNames(Collection<String> tagIds, String ownerUserId) {
        if (tagIds == null || tagIds.isEmpty()) return Collections.emptyMap();

        Set<TagId> ids = new LinkedHashSet<>();
        for (String tagId : tagIds) {
            if (tagId == null || tagId.trim().isEmpty()) continue;
            ids.add(TagId.of(tagId));
        }
        if (ids.isEmpty()) return Collections.emptyMap();

        Map<TagId, String> fetched = tagQueryService.getNamesByIds(ownerUserId, ids);
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<TagId, String> entry : fetched.entrySet()) {
            result.put(entry.getKey().getValue(), entry.getValue());
        }
        return result;
    }
}
