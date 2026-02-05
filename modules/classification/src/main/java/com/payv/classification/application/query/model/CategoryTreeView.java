package com.payv.classification.application.query.model;

import com.payv.classification.domain.model.Category;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class CategoryTreeView {

    private final String categoryId;
    private final String name;
    private final List<CategoryChildView> children;

    private CategoryTreeView(String categoryId, String name, List<CategoryChildView> children) {
        this.categoryId = categoryId;
        this.name = name;
        this.children = children;
    }

    public static CategoryTreeView from(Category root) {
        List<CategoryChildView> children = root.getChildren() == null
                ? Collections.emptyList()
                : root.getChildren().stream()
                .map(c -> new CategoryChildView(c.getId().getValue(), c.getName()))
                .collect(Collectors.toList());

        return new CategoryTreeView(root.getId().getValue(), root.getName(), children);
    }
}
