package com.payv.ledger.application.port;

import com.payv.contracts.common.dto.IdNamePublicDto;
import com.payv.contracts.classification.dto.CategoryTreePublicDto;

import java.util.Collection;
import java.util.List;

public interface ClassificationQueryPort {
    List<IdNamePublicDto> getTagNames(Collection<String> tagIds, String ownerUserId);
    List<IdNamePublicDto> getCategoryNames(Collection<String> categoryIds, String ownerUserId);
    List<IdNamePublicDto> getAllTags(String ownerUserId);
    List<CategoryTreePublicDto> getAllCategories(String ownerUserId);
}
