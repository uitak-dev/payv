package com.payv.classification.presentation.dto.request;

import com.payv.classification.application.command.model.CreateChildCategoryCommand;
import com.payv.classification.domain.model.CategoryId;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public final class CreateChildCategoryRequest {

    @NotBlank
    private String name;

    public CreateChildCategoryCommand toCommand(String rootId) {
        return new CreateChildCategoryCommand(CategoryId.of(rootId), name);
    }
}
