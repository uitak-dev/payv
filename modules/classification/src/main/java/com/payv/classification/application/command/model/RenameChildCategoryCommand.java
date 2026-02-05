package com.payv.classification.application.command.model;

import com.payv.classification.domain.model.CategoryId;
import lombok.Getter;

@Getter
public class RenameChildCategoryCommand {
    private final CategoryId rootId;
    private final CategoryId childId;
    private final String newName;

    public RenameChildCategoryCommand(CategoryId rootId, CategoryId childId, String newName) {
        this.rootId = rootId;
        this.childId = childId;
        this.newName = newName;
    }
}
