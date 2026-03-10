package com.payv.budget.application.port;

import com.payv.contracts.classification.dto.CategoryTreePublicDto;
import com.payv.contracts.common.dto.IdNamePublicDto;

import java.util.Collection;
import java.util.List;

public interface ClassificationQueryPort {
    List<IdNamePublicDto> getCategoryNames(Collection<String> categoryIds, String ownerUserId);
    List<CategoryTreePublicDto> getAllCategories(String ownerUserId);
}
