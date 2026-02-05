package com.payv.classification.application.command.model;

import lombok.Getter;

@Getter
public class CreateParentCategoryCommand {
    private final String name;

    public CreateParentCategoryCommand(String name) { this.name = name; }
}
