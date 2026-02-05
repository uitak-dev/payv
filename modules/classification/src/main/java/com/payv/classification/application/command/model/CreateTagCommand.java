package com.payv.classification.application.command.model;

import com.payv.classification.domain.model.TagId;
import lombok.Getter;

@Getter
public class CreateTagCommand {
    private final String name;
    public CreateTagCommand(String name) { this.name = name; }
}
