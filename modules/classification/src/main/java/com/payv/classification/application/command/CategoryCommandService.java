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
public class CategoryCommandService {

    private final CategoryRepository categoryRepository;

    public CategoryId createParent(CreateParentCategoryCommand command, String ownerUserId) {
        Objects.requireNonNull(command, "command");
        requireOwner(ownerUserId);

        int current = categoryRepository.countParents(ownerUserId);
        Category.assertCanCreateNewRoot(current);

        // 이름 중복 확인.
        ensureUniqueRootName(ownerUserId, command.getName(), null);

        Category root = Category.createParent(ownerUserId, command.getName());
        categoryRepository.save(root, ownerUserId);
        return root.getId();
    }

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

    public void renameRoot(RenameRootCategoryCommand command, String ownerUserId) {
        Objects.requireNonNull(command, "command");
        requireOwner(ownerUserId);

        Category root = categoryRepository.findRootById(command.getRootId(), ownerUserId)
                .orElseThrow(() -> new CategoryNotFoundException("root category not found"));

        ensureUniqueRootName(ownerUserId, command.getNewName(), root.getId());

        root.renameRoot(command.getNewName());
        categoryRepository.save(root, ownerUserId);
    }

    public void renameChild(RenameChildCategoryCommand command, String ownerUserId) {
        Objects.requireNonNull(command, "command");
        requireOwner(ownerUserId);

        Category root = categoryRepository.findRootById(command.getRootId(), ownerUserId)
                .orElseThrow(() -> new CategoryNotFoundException("root category not found"));

        root.renameChild(command.getChildId(), command.getNewName());
        categoryRepository.save(root, ownerUserId);
    }

    public void deactivateRoot(DeactivateRootCategoryCommand command, String ownerUserId) {
        Objects.requireNonNull(command, "command");
        requireOwner(ownerUserId);

        Category root = categoryRepository.findRootById(command.getRootId(), ownerUserId)
                .orElseThrow(() -> new CategoryNotFoundException("root category not found"));

        root.deactivateRoot(); // 도메인 정책(시스템 금지 등)에서 막아야 함
        categoryRepository.save(root, ownerUserId);
    }

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
