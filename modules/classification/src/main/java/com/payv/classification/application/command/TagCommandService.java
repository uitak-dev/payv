package com.payv.classification.application.command;

import com.payv.classification.application.command.model.CreateTagCommand;
import com.payv.classification.application.command.model.DeactivateTagCommand;
import com.payv.classification.application.command.model.RenameTagCommand;
import com.payv.classification.application.exception.DuplicateTagNameException;
import com.payv.classification.application.exception.TagNotFoundException;
import com.payv.classification.domain.model.Tag;
import com.payv.classification.domain.model.TagId;
import com.payv.classification.domain.repository.TagRepository;
import com.payv.common.error.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class TagCommandService {

    private final TagRepository tagRepository;

    public TagId create(CreateTagCommand command, String ownerUserId) {
        Objects.requireNonNull(command, "command");
        requireOwner(ownerUserId);

        int current = tagRepository.countTags(ownerUserId);
        Tag.assertCanCreateNewTag(current);

        ensureUniqueTagName(ownerUserId, command.getName(), null);

        Tag tag = Tag.create(ownerUserId, command.getName());
        tagRepository.save(tag, ownerUserId);
        return tag.getId();
    }

    public void rename(RenameTagCommand command, String ownerUserId) {
        Objects.requireNonNull(command, "command");
        requireOwner(ownerUserId);

        Tag tag = tagRepository.findById(command.getTagId(), ownerUserId)
                .orElseThrow(TagNotFoundException::new);

        ensureUniqueTagName(ownerUserId, command.getNewName(), tag.getId());

        tag.rename(command.getNewName());
        tagRepository.save(tag, ownerUserId);
    }

    public void deactivate(DeactivateTagCommand command, String ownerUserId) {
        Objects.requireNonNull(command, "command");
        requireOwner(ownerUserId);

        Tag tag = tagRepository.findById(command.getTagId(), ownerUserId)
                .orElseThrow(TagNotFoundException::new);

        tag.deactivate();
        tagRepository.save(tag, ownerUserId);
    }

    private void ensureUniqueTagName(String ownerUserId, String name, TagId excludeId) {
        String normalized = Tag.normalizeName(name);
        tagRepository.findAllByOwner(ownerUserId).forEach(existing -> {
            if (excludeId != null && existing.getId().equals(excludeId)) return;
            if (existing.getName().equals(normalized)) {
                throw new DuplicateTagNameException();
            }
        });
    }

    private static void requireOwner(String ownerUserId) {
        if (ownerUserId == null || ownerUserId.trim().isEmpty()) {
            throw new InvalidRequestException("ownerUserId must not be blank");
        }
    }
}
