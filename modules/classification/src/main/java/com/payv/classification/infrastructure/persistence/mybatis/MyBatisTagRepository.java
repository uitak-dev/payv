package com.payv.classification.infrastructure.persistence.mybatis;

import com.payv.classification.domain.model.Tag;
import com.payv.classification.domain.model.TagId;
import com.payv.classification.domain.repository.TagRepository;
import com.payv.classification.infrastructure.persistence.mybatis.mapper.TagMapper;
import com.payv.classification.infrastructure.persistence.mybatis.record.TagRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class MyBatisTagRepository implements TagRepository {

    private final TagMapper tagMapper;

    @Override
    public void save(Tag tag, String ownerUserId) {
        tagMapper.upsert(TagRecord.toRecord(tag));
    }

    @Override
    public void delete(TagId tagId) {
        tagMapper.deleteById(tagId.getValue());
    }

    @Override
    public Optional<Tag> findById(TagId tagId, String ownerUserId) {
        TagRecord record = tagMapper.selectByIdAndOwner(tagId.getValue(), ownerUserId);
        if (record == null) return Optional.empty();

        return Optional.of(record.toEntity());
    }

    @Override
    public List<Tag> findAllByOwner(String ownerUserId) {
        List<TagRecord> records = tagMapper.selectAllByOwner(ownerUserId);
        if (records == null || records.isEmpty()) return Collections.emptyList();

        return records.stream()
                .map(r -> r.toEntity())
                .collect(Collectors.toList());
    }

    @Override
    public int countTags(String ownerUserId) {
        return tagMapper.countByOwner(ownerUserId);
    }

    @Override
    public Map<TagId, String> findNamesByIds(String ownerUserId, Collection<TagId> tagIds) {
        if (tagIds == null || tagIds.isEmpty())
            return Collections.emptyMap();

        List<String> ids = tagIds.stream()
                .filter(Objects::nonNull)
                .map(id -> id.getValue())
                .collect(Collectors.toList());

        List<TagRecord> records = tagMapper.selectAllByOwner(ownerUserId);

        Map<TagId, String> ret = new HashMap<>();
        for (TagRecord r : records) {
            if (ids.contains(r.getTagId())) {
                ret.put(TagId.of(r.getTagId()), r.getName());
            }
        }
        return ret;
    }
}
