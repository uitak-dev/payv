package com.payv.classification.presentation.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public final class RenameChildCategoryRequest {

    @NotBlank
    private String newName;
}
