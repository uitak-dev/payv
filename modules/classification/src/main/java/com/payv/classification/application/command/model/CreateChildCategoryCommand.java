package com.payv.classification.application.command.model;

import com.payv.classification.domain.model.CategoryId;
import lombok.Getter;

@Getter
public class CreateChildCategoryCommand {
    private final CategoryId parentId;
    private final String name;

    public CreateChildCategoryCommand(CategoryId parentId, String name) {
        this.parentId = parentId;
        this.name = name;
    }
}
