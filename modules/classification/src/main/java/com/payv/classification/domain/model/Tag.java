package com.payv.classification.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.util.Objects;

@Getter
public class Tag {

    private static final int MAX_NAME_LENGTH = 20;
    private static final int MAX_CNT = 8;

    private TagId id;
    private String ownerUserId;
    private String name;
    private boolean isActive;

    @Builder
    private Tag(TagId id, String ownerUserId, String name, boolean isActive) {
        this.id = id;
        this.ownerUserId = ownerUserId;
        this.name = name;
        this.isActive = isActive;
    }

    public static Tag create(String ownerUserId, String name) {
        return Tag.builder()
                .id(TagId.generate())
                .ownerUserId(ownerUserId)
                .name(name)
                .isActive(true)
                .build();
    }

    public static Tag of(TagId id, String ownerUserId, String name, boolean isActive) {
        return Tag.builder()
                .id(id)
                .ownerUserId(ownerUserId)
                .name(name)
                .isActive(isActive)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tag)) return false;
        Tag tag = (Tag) o;
        return Objects.equals(id, tag.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /** * * * * * * * * * * * * * * * * *  *
     * Policy / Commands (domain behavior) *
     * * * * * * * * * * * * * * * * * * * */
    public void rename(String newName) {
        requireActive();
        this.name = normalizeName(newName);
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void ensureBelongsTo(String requesterOwnerUserId) {
        if (!Objects.equals(this.ownerUserId, requesterOwnerUserId)) {
            throw new IllegalStateException("tag owner mismatch");
        }
    }


    /** * * * * * * * * * * *
     * Internal validations *
     * * * * * * * * * * *  */

    private void requireActive() {
        if (!isActive) {
            throw new IllegalStateException("inactive category");
        }
    }

    public static void assertCanCreateNewTag(int existingTagCount) {
        if (existingTagCount + 1 > MAX_CNT) {
            throw new IllegalStateException("max tags exceeded: " + MAX_CNT);
        }
    }

    public static String normalizeName(String name) {
        String ret = (name == null) ? null : name.trim();
        if (ret == null || ret.isEmpty())
            throw new IllegalArgumentException("name must not be blank");
        if (ret.length() > MAX_NAME_LENGTH)
            throw new IllegalArgumentException("tag name length must be <= " + MAX_NAME_LENGTH);

        // 추후, 길이 제한/금칙어/특수문자 정책 추가.
        return ret;
    }
}
