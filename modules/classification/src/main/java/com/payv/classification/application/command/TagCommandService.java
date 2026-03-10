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
/**
 * Classification BC의 태그 변경 명령을 처리하는 서비스.
 * - 태그 생성/이름 변경/비활성화를 수행한다.
 * - 태그 최대 개수 및 이름 중복 규칙을 한 곳에서 강제해, 거래 태그 분류 체계의 일관성을 보장한다.
 */
public class TagCommandService {

    private final TagRepository tagRepository;

    /**
     * 태그를 생성한다.
     *
     * @param command 생성 요청(태그명)
     * @param ownerUserId 소유 사용자 ID
     * @return 생성된 태그 ID
     * @throws NullPointerException {@code command}가 {@code null}인 경우
     * @throws InvalidRequestException {@code ownerUserId}가 비어 있는 경우
     * @throws DuplicateTagNameException 태그 이름이 중복된 경우
     */
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

    /**
     * 태그 이름을 변경한다.
     *
     * @param command 변경 요청(태그 ID, 새 이름)
     * @param ownerUserId 소유 사용자 ID
     * @throws NullPointerException {@code command}가 {@code null}인 경우
     * @throws InvalidRequestException {@code ownerUserId}가 비어 있는 경우
     * @throws TagNotFoundException 태그를 찾지 못한 경우
     * @throws DuplicateTagNameException 새 이름이 중복된 경우
     */
    public void rename(RenameTagCommand command, String ownerUserId) {
        Objects.requireNonNull(command, "command");
        requireOwner(ownerUserId);

        Tag tag = tagRepository.findById(command.getTagId(), ownerUserId)
                .orElseThrow(TagNotFoundException::new);

        ensureUniqueTagName(ownerUserId, command.getNewName(), tag.getId());

        tag.rename(command.getNewName());
        tagRepository.save(tag, ownerUserId);
    }

    /**
     * 태그를 비활성화한다.
     *
     * @param command 비활성화 요청(태그 ID)
     * @param ownerUserId 소유 사용자 ID
     * @throws NullPointerException {@code command}가 {@code null}인 경우
     * @throws InvalidRequestException {@code ownerUserId}가 비어 있는 경우
     * @throws TagNotFoundException 태그를 찾지 못한 경우
     */
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
