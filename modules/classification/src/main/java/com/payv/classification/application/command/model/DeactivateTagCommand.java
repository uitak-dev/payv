package com.payv.classification.application.command.model;

import com.payv.classification.domain.model.TagId;
import lombok.Getter;

@Getter
public class DeactivateTagCommand {

    private final TagId tagId;

    public DeactivateTagCommand(TagId tagId) { this.tagId = tagId; }
}
