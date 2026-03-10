package com.payv.classification.application.query;

import com.payv.classification.domain.model.Category;
import com.payv.classification.domain.model.CategoryId;
import com.payv.classification.domain.model.Tag;
import com.payv.classification.domain.model.TagId;
import com.payv.classification.domain.repository.CategoryRepository;
import com.payv.classification.domain.repository.TagRepository;
import com.payv.contracts.classification.ClassificationPublicApi;
import com.payv.contracts.classification.dto.CategoryTreePublicDto;
import com.payv.contracts.common.dto.IdNamePublicDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClassificationPublicService implements ClassificationPublicApi {

    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    /**
     * 카테고리 ID 집합에 대한 ID/이름 DTO 목록을 조회한다.
     *
     * @param ownerUserId 소유 사용자 ID
     * @param categoryIds 조회 대상 카테고리 ID들
     * @return 카테고리 ID/이름 DTO 목록
     */
    @Override
    public List<IdNamePublicDto> getCategoriesByIds(String ownerUserId, Collection<String> categoryIds) {
        if (ownerUserId == null || ownerUserId.trim().isEmpty() || categoryIds == null || categoryIds.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> normalizedIds = new LinkedHashSet<>();
        for (String categoryId : categoryIds) {
            if (categoryId == null || categoryId.trim().isEmpty()) {
                continue;
            }
            normalizedIds.add(categoryId.trim());
        }
        if (normalizedIds.isEmpty()) {
            return Collections.emptyList();
        }

        Set<CategoryId> ids = new LinkedHashSet<>();
        for (String normalizedId : normalizedIds) {
            ids.add(CategoryId.of(normalizedId));
        }

        List<Category> fetched = categoryRepository.findNamesByIds(ownerUserId, ids);
        Map<String, String> names = new LinkedHashMap<>();
        for (Category category : fetched) {
            names.put(category.getId().getValue(), category.getName());
        }

        List<IdNamePublicDto> result = new ArrayList<>();
        for (String normalizedId : normalizedIds) {
            String name = names.get(normalizedId);
            if (name != null) {
                result.add(new IdNamePublicDto(normalizedId, name));
            }
        }
        return result;
    }

    /**
     * 태그 ID 집합의 ID/이름 DTO 목록을 조회한다.
     *
     * @param ownerUserId 소유 사용자 ID
     * @param tagIds 조회 대상 태그 ID들
     * @return 태그 ID/이름 DTO 목록
     */
    @Override
    public List<IdNamePublicDto> getTagsByIds(String ownerUserId, Collection<String> tagIds) {
        if (ownerUserId == null || ownerUserId.trim().isEmpty() || tagIds == null || tagIds.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> normalizedIds = new LinkedHashSet<>();
        for (String tagId : tagIds) {
            if (tagId == null || tagId.trim().isEmpty()) {
                continue;
            }
            normalizedIds.add(tagId.trim());
        }
        if (normalizedIds.isEmpty()) {
            return Collections.emptyList();
        }

        Set<TagId> ids = new LinkedHashSet<>();
        for (String normalizedId : normalizedIds) {
            ids.add(TagId.of(normalizedId));
        }

        List<Tag> fetched = tagRepository.findNamesByIds(ownerUserId, ids);
        Map<String, String> names = new LinkedHashMap<>();
        for (Tag tag : fetched) {
            names.put(tag.getId().getValue(), tag.getName());
        }

        List<IdNamePublicDto> result = new ArrayList<>();
        for (String normalizedId : normalizedIds) {
            String name = names.get(normalizedId);
            if (name != null) {
                result.add(new IdNamePublicDto(normalizedId, name));
            }
        }
        return result;
    }

    @Override
    public List<CategoryTreePublicDto> getAllCategoryTrees(String ownerUserId) {
        if (ownerUserId == null || ownerUserId.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<Category> roots = categoryRepository.findAllCategory(ownerUserId);
        if (roots == null || roots.isEmpty()) {
            return Collections.emptyList();
        }

            List<CategoryTreePublicDto> result = new ArrayList<>(roots.size());
        for (Category root : roots) {
            List<IdNamePublicDto> children = new ArrayList<>();
            if (root.getChildren() != null) {
                for (Category child : root.getChildren()) {
                    children.add(new IdNamePublicDto(
                            child.getId().getValue(),
                            child.getName()
                    ));
                }
            }
            result.add(new CategoryTreePublicDto(
                    root.getId().getValue(),
                    root.getName(),
                    children
            ));
        }
        return result;
    }

    @Override
    public List<IdNamePublicDto> getAllTags(String ownerUserId) {
        if (ownerUserId == null || ownerUserId.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<Tag> tags = tagRepository.findAllByOwner(ownerUserId);
        if (tags == null || tags.isEmpty()) {
            return Collections.emptyList();
        }

        List<IdNamePublicDto> result = new ArrayList<>(tags.size());
        for (Tag tag : tags) {
            result.add(new IdNamePublicDto(tag.getId().getValue(), tag.getName()));
        }
        return result;
    }
}
