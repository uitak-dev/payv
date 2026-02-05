package com.payv.classification.domain.repository;

import com.payv.classification.domain.model.Category;
import com.payv.classification.domain.model.CategoryId;

import java.util.*;

public interface CategoryRepository {

    /**
     * Aggregate Root 저장.
     * - 입력 Category는 반드시 depth=1(root)이며,
     * - 내부 children(depth=2)을 포함한 AR 전체를 저장한다.
     */
    void save(Category rootCategory, String ownerUserId);

    // parentId 로, AR 로드(children 포함).
    Optional<Category> findRootById(CategoryId parentId, String ownerUserId);

    // 전체 카테고리 목록 로드.( 각 하위 카테고리 목록 포함 )
    List<Category> findAllCategory(String ownerUserId);

    // 전체 상위 카테고리(parent) 목록 로드.
    List<Category> findAllParentByOwner(String ownerUserId);

    // 전체 상위 카테고리(parent) 수.
    int countParents(String ownerUserId);

    // 특정 parent의 children 수.
    int countChildren(CategoryId parentId, String ownerUserId);

    /**
     * Ledger 연동: id -> name 매핑 반환.
     * - Transaction 조회 응답 조립 시 사용.
     */
    Map<CategoryId, String> findNamesByIds(String ownerUserId, Collection<CategoryId> categoryIds);

}
