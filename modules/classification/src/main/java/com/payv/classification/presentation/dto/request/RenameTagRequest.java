package com.payv.classification.presentation.dto.request;

import com.payv.classification.application.command.model.RenameTagCommand;
import com.payv.classification.domain.model.TagId;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public final class RenameTagRequest {

    @NotBlank
    private String newName;

    public RenameTagCommand toCommand(String tagId) {
        return new RenameTagCommand(TagId.of(tagId), newName);
    }
}
