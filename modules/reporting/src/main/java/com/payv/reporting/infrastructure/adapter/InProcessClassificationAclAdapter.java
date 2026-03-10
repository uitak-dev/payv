package com.payv.reporting.infrastructure.adapter;

import com.payv.contracts.classification.ClassificationPublicApi;
import com.payv.contracts.common.dto.IdNamePublicDto;
import com.payv.reporting.application.port.ClassificationLookupPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component("reportingClassificationAclAdapter")
@RequiredArgsConstructor
public class InProcessClassificationAclAdapter implements ClassificationLookupPort {

    private final ClassificationPublicApi classificationPublicService;

    @Override
    public List<IdNamePublicDto> getCategoryNames(Collection<String> categoryIds, String ownerUserId) {
        if (categoryIds == null || categoryIds.isEmpty()) return Collections.emptyList();
        return classificationPublicService.getCategoriesByIds(ownerUserId, categoryIds);
    }

    @Override
    public List<IdNamePublicDto> getTagNames(Collection<String> tagIds, String ownerUserId) {
        if (tagIds == null || tagIds.isEmpty()) return Collections.emptyList();
        return classificationPublicService.getTagsByIds(ownerUserId, tagIds);
    }
}
