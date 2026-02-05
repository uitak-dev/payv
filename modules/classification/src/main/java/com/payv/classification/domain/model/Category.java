package com.payv.classification.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
public class Category {

    private static final int MAX_DEPTH = 2;
    private static final int MAX_PARENTS_CNT = 8;
    private static final int MAX_CHILDREN_CNT = 6;

    private CategoryId id;
    private String name;
    private Category parent;
    private List<Category> children = new ArrayList<>();
    private String ownerUserId;     // null이면, 시스템 기본 카테고리
    private boolean isSystem;       // 시스템 기본 제공(수정/삭제 불가)
    private Integer depth;          // 1 or 2
    private boolean isActive;

    @Builder
    private Category(CategoryId id, String name, Category parent, String ownerUserId,
                     boolean isSystem, Integer depth, boolean isActive) {
        this.id = id;
        this.name = name;
        this.parent = parent;
        this.ownerUserId = ownerUserId;
        this.isSystem = isSystem;
        this.depth = depth;
        this.isActive = isActive;
    }

    public static Category createParent(String ownerUserId, String name) {
        return Category.builder()
                .id(CategoryId.generate())
                .name(name)
                .ownerUserId(ownerUserId)
                .isSystem(false)
                .depth(1)
                .isActive(true)
                .build();
    }

    public static Category reconstituteParent(CategoryId id, String name, String ownerUserId,
                                             boolean isSystem, Integer depth, boolean isActive) {

        // 하위 카테고리(children) 목록은 별도의 Assembler에서 재구성.
        return Category.builder()
                .id(id)
                .name(name)
                .ownerUserId(ownerUserId)
                .isSystem(isSystem)
                .depth(depth)
                .isActive(isActive)
                .build();
    }

    public static Category reconstituteChild(CategoryId id, String name, Category parent, String ownerUserId,
                                             boolean isSystem, Integer depth, boolean isActive) {

        return Category.builder()
                .id(id)
                .name(name)
                .parent(parent)
                .ownerUserId(ownerUserId)
                .isSystem(isSystem)
                .depth(depth)
                .isActive(isActive)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category)) return false;
        Category category = (Category) o;
        return Objects.equals(id, category.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


    /** * * * * * * * * * * * * * * * * *  *
     * Policy / Commands (domain behavior) *
     * * * * * * * * * * * * * * * * * * * */
    public Category createChild(String ownerUserId, String name) {
        requireCanAddChild(ownerUserId, name);
        Category child = Category.builder()
                .id(CategoryId.generate())
                .name(name)
                .parent(this)
                .ownerUserId(ownerUserId)
                .isSystem(false)
                .depth(2)
                .isActive(true)
                .build();

        this.children.add(child);
        return child;
    }

    private void rename(String newName) {
        this.name = newName;
    }

    public void renameRoot(String newName) {
        requireRootOnly();
        requireEditable();
        requireActive();

        String normalized = normalizeName(newName);
        rename(normalized);
    }

    public void renameChild(CategoryId childId, String newName) {
        requireRootOnly();
        requireEditable();
        requireActive();

        Category child = findChildOrThrow(childId);
        child.requireActive();

        String normalized = normalizeName(newName);
        ensureNoDuplicateChildName(normalized, childId);

        child.rename(normalized);
    }

    private void deactivate() {
        this.isActive = false;
    }

    public void deactivateRoot() {
        requireRootOnly();
        requireEditable();

        for (Category child : children) {
            child.deactivate();
        }
        deactivate();
    }

    public void deactivateChild(CategoryId childId) {
        requireRootOnly();
        requireEditable();

        Category child = findChildOrThrow(childId);
        child.requireEditable();
        child.deactivate();
    }

    public void ensureBelongsTo(String requesterOwnerUserId) {
        // 시스템은 누구나 사용할 수 있다(정책). 사용자 카테고리는 owner만 가능.
        if (isSystemCategory()) return;
        if (!Objects.equals(this.ownerUserId, requesterOwnerUserId)) {
            throw new IllegalStateException("category owner mismatch");
        }
    }

    /** * * * * * * * * *  *
     * Convenience / Query *
     * * * * * * * * * * * */
    public boolean isRoot() {
        return depth != null && depth == 1;
    }

    public boolean isSystemCategory() {
        return isSystem || ownerUserId == null;
    }


    /** * * * * * * * * * * *
     * Internal validations *
     * * * * * * * * * * *  */
    private void requireEditable() {
        if (isSystemCategory() || isSystem) {
            throw new IllegalStateException("system category is not editable");
        }
    }

    private void requireActive() {
        if (!isActive) {
            throw new IllegalStateException("inactive category");
        }
    }

    private void requireRootOnly() {
        if (!isRoot()) {
            throw new IllegalStateException("operation allowed only for root category");
        }
    }

    private void requireCanAddChild(String ownerUserId, String childName) {
        requireEditable();
        requireActive();
        requireRootOnly();

        // owner 일치 (시스템/사용자 혼합 금지)
        if (!Objects.equals(this.ownerUserId, ownerUserId)) {
            throw new IllegalStateException("parent owner mismatch");
        }

        ensureChildrenCountWithinLimit();
        ensureNoDuplicateChildName(normalizeName(childName), null);
    }

    private void ensureChildrenCountWithinLimit() {
        if (this.children.size() >= MAX_CHILDREN_CNT) {
            throw new IllegalStateException("max child categories exceeded: " + MAX_CHILDREN_CNT);
        }
    }

    private void ensureNoDuplicateChildName(String normalizedChildName, CategoryId exceptChildId) {
        for (Category c : children) {
            if (exceptChildId != null && c.id.equals(exceptChildId)) continue;
            if (c.name.equalsIgnoreCase(normalizedChildName)) {
                throw new IllegalStateException("duplicate child name under same root");
            }
        }
    }

    private Category findChildOrThrow(CategoryId childId) {
        requireNonNull(childId, "childId");
        for (Category c : children) {
            if (c.id.equals(childId)) return c;
        }
        throw new IllegalStateException("child category not found: " + childId);
    }

    public static String normalizeName(String name) {
        String ret = (name == null) ? null : name.trim();
        if (ret == null || ret.isEmpty()) throw new IllegalArgumentException("name must not be blank");
        // 추후, 길이 제한/금칙어/특수문자 정책 추가.
        return ret;
    }

    private static <T> T requireNonNull(T v, String name) {
        return Objects.requireNonNull(v, name + " must not be null");
    }

    public static void assertCanCreateNewRoot(int existingRootCount) {
        if (existingRootCount + 1 > MAX_PARENTS_CNT) {
            throw new IllegalStateException("max parent categories exceeded: " + MAX_PARENTS_CNT);
        }
    }

}
