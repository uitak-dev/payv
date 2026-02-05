package com.payv.classification.application.command.model;

import com.payv.classification.domain.model.CategoryId;
import lombok.Getter;

@Getter
public class DeactivateRootCategoryCommand {
    private final CategoryId rootId;

    public DeactivateRootCategoryCommand(CategoryId rootId) { this.rootId = rootId; }
}
