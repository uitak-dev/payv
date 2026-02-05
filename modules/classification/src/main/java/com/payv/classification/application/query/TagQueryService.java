package com.payv.classification.application.query;

import com.payv.classification.domain.model.Tag;
import com.payv.classification.domain.model.TagId;
import com.payv.classification.domain.repository.TagRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagQueryService {

    private final TagRepository tagRepository;

    public List<TagView> getAll(String ownerUserId) {
        List<Tag> tags = tagRepository.findAllByOwner(ownerUserId);
        return tags.stream().map(TagView::from).collect(Collectors.toList());
    }

    public Optional<TagView> get(TagId tagId, String ownerUserId) {
        return tagRepository.findById(tagId, ownerUserId).map(TagView::from);
    }

    public Map<TagId, String> getNamesByIds(String ownerUserId, Collection<TagId> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyMap();
        return tagRepository.findNamesByIds(ownerUserId, ids);
    }

    @Getter
    public static class TagView {
        private final String tagId;
        private final String name;

        private TagView(String tagId, String name) {
            this.tagId = tagId;
            this.name = name;
        }

        public static TagView from(Tag tag) {
            return new TagView(tag.getId().getValue(), tag.getName());
        }
    }
}
