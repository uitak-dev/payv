package com.payv.classification.infrastructure.persistence.mybatis;

import com.payv.classification.domain.model.Category;
import com.payv.classification.domain.model.CategoryId;
import com.payv.classification.domain.repository.CategoryRepository;
import com.payv.classification.infrastructure.persistence.mybatis.assembler.CategoryAssembler;
import com.payv.classification.infrastructure.persistence.mybatis.mapper.CategoryMapper;
import com.payv.classification.infrastructure.persistence.mybatis.record.CategoryRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class MyBatisCategoryRepository implements CategoryRepository {

    private final CategoryMapper categoryMapper;
    private final CategoryAssembler assembler = new CategoryAssembler();

    @Override
    public void save(Category category, String ownerUserId) {
        CategoryRecord rootRecord = assembler.toRecord(category, ownerUserId);
        categoryMapper.upsert(rootRecord);

        syncChildren(category, ownerUserId);
    }

    @Override
    public void delete(CategoryId categoryId) {
        categoryMapper.deleteParentAndChildrenById(categoryId.getValue());
    }

    private void syncChildren(Category root, String ownerUserId) {
        String rootId = root.getId().getValue();

        List<CategoryRecord> existingRecords = categoryMapper.selectChildrenByOwner(rootId, ownerUserId);
        Set<String> existing = existingRecords.stream()
                .map(r -> r.getCategoryId())
                .collect(Collectors.toSet());

        List<Category> incomingChildren = root.getChildren() == null ? Collections.emptyList() : root.getChildren();
        Set<String> incoming = incomingChildren.stream()
                .map(c -> c.getId().getValue())
                .collect(Collectors.toSet());

        // 삭제 대상 = existing - incoming
        existing.removeAll(incoming);
        if (!existing.isEmpty()) {
            categoryMapper.deleteByIds(new ArrayList<>(existing));
        }

        // upsert children
        for (Category child : incomingChildren) {
            CategoryRecord childRecord = assembler.toRecord(child, ownerUserId);
            categoryMapper.upsert(childRecord);
        }
    }

    @Override
    public Optional<Category> findRootById(CategoryId parentId, String ownerUserId) {
        CategoryRecord root = categoryMapper.selectRootById(parentId.getValue(), ownerUserId);
        if (root == null) return Optional.empty();

        List<CategoryRecord> children = categoryMapper.selectChildrenByOwner(root.getCategoryId(), ownerUserId);
        return Optional.ofNullable(assembler.assembleAggregate(root, children));
    }

    @Override
    public List<Category> findAllCategory(String ownerUserId) {
        List<CategoryRecord> roots = categoryMapper.selectRootsByOwner(ownerUserId);
        if (roots == null || roots.isEmpty()) return Collections.emptyList();

        List<Category> result = new ArrayList<>(roots.size());
        for (CategoryRecord root : roots) {
            List<CategoryRecord> children = categoryMapper.selectChildrenByOwner(root.getCategoryId(), ownerUserId);
            result.add(assembler.assembleAggregate(root, children));
        }
        return result;
    }

    @Override
    public List<Category> findAllParentByOwner(String ownerUserId) {
        // “상위만” 요구사항이므로 children 비운 shallow 엔티티로 반환.
        List<CategoryRecord> roots = categoryMapper.selectRootsByOwner(ownerUserId);
        if (roots == null || roots.isEmpty()) return Collections.emptyList();

        return roots.stream()
                .map(assembler::toEntityShallow)
                .collect(Collectors.toList());
    }

    @Override
    public int countParents(CategoryId parentId, String ownerUserId) {
        // 인터페이스의 parentId는 의미상 불필요(무시)
        return categoryMapper.countRootsByOwner(ownerUserId);
    }

    @Override
    public int countChildren(CategoryId parentId, String ownerUserId) {
        return categoryMapper.countChildrenByOwner(parentId.getValue(), ownerUserId);
    }

    @Override
    public Map<CategoryId, String> findNamesByIds(String ownerUserId, Collection<CategoryId> categoryIds) {
        return null;
    }
}
