package com.payv.classification.infrastructure.persistence.mybatis.record;

import com.payv.classification.domain.model.Tag;
import com.payv.classification.domain.model.TagId;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
public class TagRecord {

    private String tagId;
    private String ownerUserId;
    private String name;
    private boolean isActive;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @Builder
    private TagRecord(String tagId, String ownerUserId, String name, boolean isActive,
                     OffsetDateTime createdAt, OffsetDateTime updatedAt) {

        this.tagId = tagId;
        this.ownerUserId = ownerUserId;
        this.name = name;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static TagRecord toRecord(Tag tag) {
        return TagRecord.builder()
                .tagId(tag.getId().getValue())
                .ownerUserId(tag.getOwnerUserId())
                .name(tag.getName())
                .isActive(tag.isActive())
                .build();
    }

    public Tag toEntity() {
        return Tag.of(
                TagId.of(tagId),
                ownerUserId,
                name,
                Boolean.TRUE.equals(isActive)    // null-safe 보장.
        );
    }
}
