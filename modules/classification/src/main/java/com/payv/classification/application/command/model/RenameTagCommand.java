package com.payv.classification.application.command.model;

import com.payv.classification.domain.model.TagId;
import lombok.Getter;

@Getter
public class RenameTagCommand {
    private final TagId tagId;
    private final String newName;

    public RenameTagCommand(TagId tagId, String newName) {
        this.tagId = tagId;
        this.newName = newName;
    }
}
