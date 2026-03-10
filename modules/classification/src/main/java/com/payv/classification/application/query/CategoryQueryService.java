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
/**
 * 카테고리 조회 서비스.
 * - 카테고리 트리 목록, 루트 단건, ID 기반 이름 조회를 제공한다.
 * - UI/ACL에서 계층형 카테고리 정보를 일관된 형태로 재사용한다.
 */
public class CategoryQueryService {

    private final CategoryRepository categoryRepository;

    /**
     * 소유자의 전체 카테고리 트리를 조회한다.
     *
     * @param ownerUserId 소유 사용자 ID
     * @return 루트-자식 구조의 카테고리 트리 뷰 목록
     */
    public List<CategoryTreeView> getAll(String ownerUserId) {
        List<Category> roots = categoryRepository.findAllCategory(ownerUserId);
        return roots.stream().map(CategoryTreeView::from).collect(Collectors.toList());
    }

    /**
     * 루트 카테고리 단건을 조회한다.
     *
     * @param rootId      루트 카테고리 ID
     * @param ownerUserId 소유 사용자 ID
     * @return 카테고리 트리 뷰. 없으면 {@link Optional#empty()}
     */
    public Optional<CategoryTreeView> getRoot(CategoryId rootId, String ownerUserId) {
        return categoryRepository.findRootById(rootId, ownerUserId).map(CategoryTreeView::from);
    }

}
