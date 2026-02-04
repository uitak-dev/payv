package com.payv.classification.domain.repository;

import com.payv.classification.domain.model.Tag;
import com.payv.classification.domain.model.TagId;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TagRepository {

    void save(Tag tag, String ownerUserId);

    void delete(TagId tagId);

    Optional<Tag> findById(TagId tagId, String ownerUserId);

    List<Tag> findAllByOwner(String ownerUserId);

    int countTags(String ownerUserId);

    /**
     * Ledger 연동: id -> name 매핑 반환.
     */
    Map<TagId, String> findNamesByIds(String ownerUserId, Collection<TagId> tagIds);
}
