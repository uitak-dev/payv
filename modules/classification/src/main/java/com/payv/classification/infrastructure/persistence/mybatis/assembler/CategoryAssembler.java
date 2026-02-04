package com.payv.classification.infrastructure.persistence.mybatis.assembler;

import com.payv.classification.domain.model.Category;
import com.payv.classification.domain.model.CategoryId;
import com.payv.classification.infrastructure.persistence.mybatis.record.CategoryRecord;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CategoryAssembler {

    public CategoryRecord toRecord(Category category, String ownerUserId) {
        Objects.requireNonNull(category, "category");
        CategoryRecord r = new CategoryRecord();
        r.setCategoryId(category.getId().getValue());
        r.setOwnerUserId(ownerUserId);
        r.setName(category.getName());
        r.setParentId(category.getParent() == null ? null : category.getParent().getId().getValue());
        r.setDepth(category.getDepth());
        r.setIsSystem(category.isSystem());
        r.setIsActive(category.isActive());
        return r;
    }

    public Category assembleAggregate(CategoryRecord parentRecord, List<CategoryRecord> childRecords) {
        if (parentRecord == null) return null;

        // 1) 상위(parent) 카테고리 복원
        Category parent = Category.reconstituteParent(
                CategoryId.of(parentRecord.getCategoryId()),
                parentRecord.getName(),
                parentRecord.getOwnerUserId(),
                Boolean.TRUE.equals(parentRecord.getIsSystem()),    // null-safe 보장.
                parentRecord.getDepth(),
                Boolean.TRUE.equals(parentRecord.getIsActive())     // null-safe 보장.
        );

        // 2) 하위(child) 카테고리 복원 후, parent.children에 추가
        if (childRecords != null) {
            for (CategoryRecord cr : childRecords) {
                Category child = Category.reconstituteChild(
                        CategoryId.of(cr.getCategoryId()),
                        cr.getName(),
                        parent,
                        cr.getOwnerUserId(),
                        Boolean.TRUE.equals(cr.getIsSystem()),      // null-safe 보장.
                        cr.getDepth(),
                        Boolean.TRUE.equals(cr.getIsActive())       // null-safe 보장.
                );
                parent.getChildren().add(child);
            }
        }
        return parent;
    }

    // 카테고리 간의 연관정보 없이, 카테고리 자체의 단순 정보만 포함.
    public Category toEntityShallow(CategoryRecord record) {
        if (record == null) return null;
        return Category.reconstituteParent(
                CategoryId.of(record.getCategoryId()),
                record.getName(),
                record.getOwnerUserId(),
                Boolean.TRUE.equals(record.getIsSystem()),
                record.getDepth(),
                Boolean.TRUE.equals(record.getIsActive())
        );
    }
}
