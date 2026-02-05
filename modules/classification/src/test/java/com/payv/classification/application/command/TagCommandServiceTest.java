package com.payv.classification.application.command;

import com.payv.classification.application.command.model.CreateTagCommand;
import com.payv.classification.application.command.model.DeactivateTagCommand;
import com.payv.classification.application.command.model.RenameTagCommand;
import com.payv.classification.domain.model.Tag;
import com.payv.classification.domain.model.TagId;
import com.payv.classification.domain.repository.TagRepository;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class TagCommandServiceTest {

    private static final String OWNER = "user-1";

    private InMemoryTagRepository repository;
    private TagCommandService service;

    @Before
    public void setUp() {
        repository = new InMemoryTagRepository();
        service = new TagCommandService(repository);
    }

    @Test
    public void create_createsTag() {
        // Given
        CreateTagCommand command = new CreateTagCommand("Cafe");

        // When
        TagId id = service.create(command, OWNER);

        // Then
        Tag saved = repository.findById(id, OWNER).orElse(null);
        assertNotNull(saved);
        assertEquals("Cafe", saved.getName());
        assertTrue(saved.isActive());
    }

    @Test(expected = IllegalStateException.class)
    public void create_rejectsDuplicateName() {
        // Given
        repository.save(TagTestDataBuilder.tag("Cafe"), OWNER);

        // When
        service.create(new CreateTagCommand("Cafe"), OWNER);
    }

    @Test
    public void rename_updatesName() {
        // Given
        Tag tag = TagTestDataBuilder.tag("Coffee");
        repository.save(tag, OWNER);

        // When
        service.rename(new RenameTagCommand(tag.getId(), "Cafe"), OWNER);

        // Then
        Tag saved = repository.findById(tag.getId(), OWNER).orElse(null);
        assertNotNull(saved);
        assertEquals("Cafe", saved.getName());
    }

    @Test
    public void deactivate_setsInactive() {
        // Given
        Tag tag = TagTestDataBuilder.tag("Food");
        repository.save(tag, OWNER);

        // When
        service.deactivate(new DeactivateTagCommand(tag.getId()), OWNER);

        // Then
        Tag saved = repository.findById(tag.getId(), OWNER).orElse(null);
        assertNotNull(saved);
        assertFalse(saved.isActive());
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
        public Map<TagId, String> findNamesByIds(String ownerUserId, Collection<TagId> tagIds) {
            Map<TagId, String> result = new HashMap<>();
            if (tagIds == null || tagIds.isEmpty()) return result;

            for (Tag tag : findAllByOwner(ownerUserId)) {
                if (tagIds.contains(tag.getId())) {
                    result.put(tag.getId(), tag.getName());
                }
            }
            return result;
        }
    }
}
