package com.payv.contracts.classification;

import com.payv.contracts.classification.dto.CategoryTreePublicDto;
import com.payv.contracts.common.dto.IdNamePublicDto;

import java.util.Collection;
import java.util.List;

public interface ClassificationPublicApi {

    List<IdNamePublicDto> getCategoriesByIds(String ownerUserId, Collection<String> categoryIds);

    List<IdNamePublicDto> getTagsByIds(String ownerUserId, Collection<String> tagIds);

    List<CategoryTreePublicDto> getAllCategoryTrees(String ownerUserId);

    List<IdNamePublicDto> getAllTags(String ownerUserId);
}
