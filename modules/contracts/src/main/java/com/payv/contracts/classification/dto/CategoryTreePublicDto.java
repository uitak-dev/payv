package com.payv.contracts.classification.dto;

import com.payv.contracts.common.dto.IdNamePublicDto;

import java.util.List;

public class CategoryTreePublicDto {

    private final String categoryId;
    private final String name;
    private final List<IdNamePublicDto> children;

    public CategoryTreePublicDto(String categoryId, String name, List<IdNamePublicDto> children) {
        this.categoryId = categoryId;
        this.name = name;
        this.children = children;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getName() {
        return name;
    }

    public List<IdNamePublicDto> getChildren() {
        return children;
    }
}
