package com.payv.classification.application.query;

import com.payv.classification.application.query.model.CategoryTreeView;
import com.payv.classification.domain.model.Category;
import com.payv.classification.domain.model.CategoryId;
import com.payv.classification.domain.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryQueryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryTreeView> getAll(String ownerUserId) {
        List<Category> roots = categoryRepository.findAllCategory(ownerUserId);
        return roots.stream().map(CategoryTreeView::from).collect(Collectors.toList());
    }

    public Optional<CategoryTreeView> getRoot(CategoryId rootId, String ownerUserId) {
        return categoryRepository.findRootById(rootId, ownerUserId).map(CategoryTreeView::from);
    }

    public Map<CategoryId, String> getNamesByIds(String ownerUserId, Collection<CategoryId> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyMap();
        return categoryRepository.findNamesByIds(ownerUserId, ids);
    }

}
