package com.payv.ledger.application.port;

import com.payv.ledger.application.port.dto.CategoryTreeOptionDto;
import com.payv.ledger.application.port.dto.TagOptionDto;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ClassificationQueryPort {
    Map<String, String> getTagNames(Collection<String> tagIds, String ownerUserId);
    Map<String, String> getCategoryNames(Collection<String> categoryIds, String ownerUserId);
    List<TagOptionDto> getAllTags(String ownerUserId);
    List<CategoryTreeOptionDto> getAllCategories(String ownerUserId);
}
