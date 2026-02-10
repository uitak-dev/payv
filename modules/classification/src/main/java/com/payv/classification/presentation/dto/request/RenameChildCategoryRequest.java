package com.payv.classification.presentation.dto.request;

import com.payv.classification.application.command.model.RenameChildCategoryCommand;
import com.payv.classification.domain.model.CategoryId;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public final class RenameChildCategoryRequest {

    @NotBlank
    private String newName;

    public RenameChildCategoryCommand toCommand(String rootId, String childId) {
        return new RenameChildCategoryCommand(CategoryId.of(rootId), CategoryId.of(childId), newName);
    }
}
