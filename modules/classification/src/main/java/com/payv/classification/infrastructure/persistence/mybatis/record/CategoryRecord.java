package com.payv.classification.infrastructure.persistence.mybatis.record;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class CategoryRecord {

    private String categoryId;
    private String ownerUserId;     // 시스템 카테고리면 null 가능(정책에 따라)
    private String name;
    private String parentId;        // root면 null
    private Integer depth;          // 1 or 2
    private Boolean isSystem;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    private CategoryRecord(String categoryId, String ownerUserId, String name, String parentId,
                          Integer depth, Boolean isSystem, Boolean isActive,
                          LocalDateTime createdAt, LocalDateTime updatedAt) {

        this.categoryId = categoryId;
        this.ownerUserId = ownerUserId;
        this.name = name;
        this.parentId = parentId;
        this.depth = depth;
        this.isSystem = isSystem;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
