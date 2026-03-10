package com.payv.classification.application.query.model;

import com.payv.classification.domain.model.Tag;
import lombok.Getter;

@Getter
public class TagView {
    private final String tagId;
    private final String name;

    private TagView(String tagId, String name) {
        this.tagId = tagId;
        this.name = name;
    }

    public static TagView from(Tag tag) {
        return new TagView(tag.getId().getValue(), tag.getName());
    }
}
