package com.payv.automation.application.port;

import com.payv.automation.application.port.dto.CategoryTreeOptionDto;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ClassificationQueryPort {

    Map<String, String> getCategoryNames(Collection<String> categoryIds, String ownerUserId);

    List<CategoryTreeOptionDto> getAllCategories(String ownerUserId);
}
