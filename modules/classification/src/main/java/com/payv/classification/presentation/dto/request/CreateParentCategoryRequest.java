package com.payv.classification.presentation.dto.request;

import com.payv.classification.application.command.model.CreateParentCategoryCommand;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public final class CreateParentCategoryRequest {

    @NotBlank
    private String name;

    public CreateParentCategoryCommand toCommand() {
        return new CreateParentCategoryCommand(name);
    }
}
