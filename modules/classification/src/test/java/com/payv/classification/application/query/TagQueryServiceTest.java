package com.payv.classification.application.query;

import com.payv.classification.application.query.model.TagView;
import com.payv.classification.domain.model.Tag;
import com.payv.classification.domain.model.TagId;
import com.payv.classification.domain.repository.TagRepository;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class TagQueryServiceTest {

    private static final String OWNER = "user-1";

    private InMemoryTagRepository repository;
    private TagQueryService service;

    @Before
    public void setUp() {
        repository = new InMemoryTagRepository();
        service = new TagQueryService(repository);
    }

    @Test
    public void getAll_returnsViews() {
        // Given
        Tag tag = TagTestDataBuilder.tag("Lunch");
        repository.save(tag, OWNER);

        // When
        List<TagView> views = service.getAll(OWNER);

        // Then
        assertEquals(1, views.size());
        assertEquals("Lunch", views.get(0).getName());
    }

    @Test
    public void get_returnsOptionalView() {
        // Given
        Tag tag = TagTestDataBuilder.tag("Snack");
        repository.save(tag, OWNER);

        // When
        Optional<TagView> view = service.get(tag.getId(), OWNER);

        // Then
        assertTrue(view.isPresent());
        assertEquals(tag.getId().getValue(), view.get().getTagId());
    }

    private static class TagTestDataBuilder {
        static Tag tag(String name) {
            return Tag.create(OWNER, name);
        }
    }

    private static class InMemoryTagRepository implements TagRepository {
        private final Map<String, Map<TagId, Tag>> storeByOwner = new HashMap<>();

        @Override
        public void save(Tag tag, String ownerUserId) {
            storeByOwner
                    .computeIfAbsent(ownerUserId, k -> new LinkedHashMap<>())
                    .put(tag.getId(), tag);
        }

        @Override
        public void delete(TagId tagId) {
            for (Map<TagId, Tag> tags : storeByOwner.values()) {
                tags.remove(tagId);
            }
        }

        @Override
        public Optional<Tag> findById(TagId tagId, String ownerUserId) {
            Map<TagId, Tag> tags = storeByOwner.get(ownerUserId);
            if (tags == null) return Optional.empty();
            return Optional.ofNullable(tags.get(tagId));
        }

        @Override
        public List<Tag> findAllByOwner(String ownerUserId) {
            Map<TagId, Tag> tags = storeByOwner.get(ownerUserId);
            if (tags == null) return Collections.emptyList();
            return new ArrayList<>(tags.values());
        }

        @Override
        public int countTags(String ownerUserId) {
            Map<TagId, Tag> tags = storeByOwner.get(ownerUserId);
            return tags == null ? 0 : tags.size();
        }

        @Override
        public List<Tag> findNamesByIds(String ownerUserId, Collection<TagId> tagIds) {
            List<Tag> result = new ArrayList<>();
            if (tagIds == null || tagIds.isEmpty()) return result;

            for (Tag tag : findAllByOwner(ownerUserId)) {
                if (tagIds.contains(tag.getId())) {
                    result.add(tag);
                }
            }
            return result;
        }
    }
}
