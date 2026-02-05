package com.payv.classification.application.command.model;

import com.payv.classification.domain.model.CategoryId;
import lombok.Getter;

@Getter
public class DeactivateChildCategoryCommand {
    private final CategoryId rootId;
    private final CategoryId childId;

    public DeactivateChildCategoryCommand(CategoryId rootId, CategoryId childId) {
        this.rootId = rootId;
        this.childId = childId;
    }
}
