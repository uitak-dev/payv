package com.payv.classification.presentation.dto.request;

import com.payv.classification.application.command.model.CreateTagCommand;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public final class CreateTagRequest {

    @NotBlank
    private String name;

    public CreateTagCommand toCommand() {
        return new CreateTagCommand(name);
    }
}
