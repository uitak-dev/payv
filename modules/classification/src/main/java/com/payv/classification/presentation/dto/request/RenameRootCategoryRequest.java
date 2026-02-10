package com.payv.classification.presentation.dto.request;

import com.payv.classification.application.command.model.RenameRootCategoryCommand;
import com.payv.classification.domain.model.CategoryId;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public final class RenameRootCategoryRequest {

    @NotBlank
    private String newName;

    public RenameRootCategoryCommand toCommand(String rootId) {
        return new RenameRootCategoryCommand(CategoryId.of(rootId), newName);
    }
}
