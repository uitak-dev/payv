package com.payv.classification.application.command;

import com.payv.classification.application.command.model.*;
import com.payv.classification.application.exception.CategoryNotFoundException;
import com.payv.classification.application.exception.DuplicateRootCategoryNameException;
import com.payv.classification.domain.model.Category;
import com.payv.classification.domain.model.CategoryId;
import com.payv.classification.domain.repository.CategoryRepository;
import com.payv.common.error.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
/**
 * 카테고리 변경 명령을 처리하는 서비스.
 * - 1-depth(루트) 및 2-depth(자식) 카테고리 생성/이름 변경/비활성화를 수행한다.
 * - 카테고리 개수 제한, 루트/자식 이름 규칙, 비활성화 정책을 도메인 규칙과 함께 관리한다.
 */
public class CategoryCommandService {

    private final CategoryRepository categoryRepository;

    /**
     * 루트 카테고리를 생성한다.
     *
     * @param command 생성 요청(루트 이름 등)
     * @param ownerUserId 카테고리 소유 사용자 ID
     * @return 생성된 루트 카테고리 ID
     * @throws NullPointerException {@code command}가 {@code null}인 경우
     * @throws InvalidRequestException {@code ownerUserId}가 비어 있는 경우
     * @throws DuplicateRootCategoryNameException 루트 이름이 중복된 경우
     */
    public CategoryId createParent(CreateParentCategoryCommand command, String ownerUserId) {
        Objects.requireNonNull(command, "command");
        requireOwner(ownerUserId);

        // 루트 카테고리 생성 제한(최대 개수) 확인.
        int current = categoryRepository.countParents(ownerUserId);
        Category.assertCanCreateNewRoot(current);

        // 이름 중복 확인.
        ensureUniqueRootName(ownerUserId, command.getName(), null);

        Category root = Category.createParent(ownerUserId, command.getName());
        categoryRepository.save(root, ownerUserId);
        return root.getId();
    }

    /**
     * 루트 하위 자식 카테고리를 생성한다.
     * - 부모 루트가 존재해야 하며,
     * - 자식 생성 제한(최대 개수/중복명)은 루트 엔티티 정책으로 검증된다.
     *
     * @param command 생성 요청(부모 ID, 자식 이름)
     * @param ownerUserId 카테고리 소유 사용자 ID
     * @return 생성된 자식 카테고리 ID
     * @throws NullPointerException {@code command}가 {@code null}인 경우
     * @throws InvalidRequestException {@code ownerUserId}가 비어 있는 경우
     * @throws CategoryNotFoundException 부모 루트 카테고리를 찾지 못한 경우
     */
    public CategoryId createChild(CreateChildCategoryCommand command, String ownerUserId) {
        Objects.requireNonNull(command, "command");
        requireOwner(ownerUserId);

        Category root = categoryRepository.findRootById(command.getParentId(), ownerUserId)
                .orElseThrow(() -> new CategoryNotFoundException("parent category not found"));

        // root 내부 정책(최대 6개, 중복명 등)은 엔티티 메서드 책임
        Category child = root.createChild(ownerUserId, command.getName());
        categoryRepository.save(root, ownerUserId);

        return child.getId();
    }

    /**
     * 루트 카테고리 이름을 변경한다.
     *
     * @param command 변경 요청(루트 ID, 새 이름)
     * @param ownerUserId 카테고리 소유 사용자 ID
     * @throws NullPointerException {@code command}가 {@code null}인 경우
     * @throws InvalidRequestException {@code ownerUserId}가 비어 있는 경우
     * @throws CategoryNotFoundException 루트 카테고리를 찾지 못한 경우
     * @throws DuplicateRootCategoryNameException 새 루트 이름이 중복된 경우
     */
    public void renameRoot(RenameRootCategoryCommand command, String ownerUserId) {
        Objects.requireNonNull(command, "command");
        requireOwner(ownerUserId);

        Category root = categoryRepository.findRootById(command.getRootId(), ownerUserId)
                .orElseThrow(() -> new CategoryNotFoundException("root category not found"));

        ensureUniqueRootName(ownerUserId, command.getNewName(), root.getId());

        root.renameRoot(command.getNewName());
        categoryRepository.save(root, ownerUserId);
    }

    /**
     * 자식 카테고리 이름을 변경한다.
     *
     * @param command 변경 요청(루트 ID, 자식 ID, 새 이름)
     * @param ownerUserId 카테고리 소유 사용자 ID
     * @throws NullPointerException {@code command}가 {@code null}인 경우
     * @throws InvalidRequestException {@code ownerUserId}가 비어 있는 경우
     * @throws CategoryNotFoundException 루트 카테고리를 찾지 못한 경우
     */
    public void renameChild(RenameChildCategoryCommand command, String ownerUserId) {
        Objects.requireNonNull(command, "command");
        requireOwner(ownerUserId);

        Category root = categoryRepository.findRootById(command.getRootId(), ownerUserId)
                .orElseThrow(() -> new CategoryNotFoundException("root category not found"));

        root.renameChild(command.getChildId(), command.getNewName());
        categoryRepository.save(root, ownerUserId);
    }

    /**
     * 루트 카테고리를 비활성화한다.
     *
     * @param command 비활성화 요청(루트 ID)
     * @param ownerUserId 카테고리 소유 사용자 ID
     * @throws NullPointerException {@code command}가 {@code null}인 경우
     * @throws InvalidRequestException {@code ownerUserId}가 비어 있는 경우
     * @throws CategoryNotFoundException 루트 카테고리를 찾지 못한 경우
     */
    public void deactivateRoot(DeactivateRootCategoryCommand command, String ownerUserId) {
        Objects.requireNonNull(command, "command");
        requireOwner(ownerUserId);

        Category root = categoryRepository.findRootById(command.getRootId(), ownerUserId)
                .orElseThrow(() -> new CategoryNotFoundException("root category not found"));

        root.deactivateRoot(); // 도메인 정책(시스템 금지 등)에서 막아야 함
        categoryRepository.save(root, ownerUserId);
    }

    /**
     * 자식 카테고리를 비활성화한다.
     *
     * @param command 비활성화 요청(루트 ID, 자식 ID)
     * @param ownerUserId 카테고리 소유 사용자 ID
     * @throws NullPointerException {@code command}가 {@code null}인 경우
     * @throws InvalidRequestException {@code ownerUserId}가 비어 있는 경우
     * @throws CategoryNotFoundException 루트 카테고리를 찾지 못한 경우
     */
    public void deactivateChild(DeactivateChildCategoryCommand command, String ownerUserId) {
        Objects.requireNonNull(command, "command");
        requireOwner(ownerUserId);

        Category root = categoryRepository.findRootById(command.getRootId(), ownerUserId)
                .orElseThrow(() -> new CategoryNotFoundException("root category not found"));

        root.deactivateChild(command.getChildId());
        categoryRepository.save(root, ownerUserId);
    }

    private void ensureUniqueRootName(String ownerUserId, String name, CategoryId excludeId) {
        String normalized = Category.normalizeName(name);
        categoryRepository.findAllParentByOwner(ownerUserId).forEach(existing -> {
            if (existing.getId().equals(excludeId)) return;
            if (existing.getName().equals(normalized)) {
                throw new DuplicateRootCategoryNameException();
            }
        });
    }

    private static void requireOwner(String ownerUserId) {
        if (ownerUserId == null || ownerUserId.trim().isEmpty()) {
            throw new InvalidRequestException("ownerUserId must not be blank");
        }
    }
}
