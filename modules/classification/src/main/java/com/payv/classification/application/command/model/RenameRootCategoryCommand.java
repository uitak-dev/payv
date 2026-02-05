package com.payv.classification.application.command.model;

import com.payv.classification.domain.model.CategoryId;
import lombok.Getter;

@Getter
public class RenameRootCategoryCommand {
    private final CategoryId rootId;
    private final String newName;

    public RenameRootCategoryCommand(CategoryId rootId, String newName) {
        this.rootId = rootId;
        this.newName = newName;
    }
}
