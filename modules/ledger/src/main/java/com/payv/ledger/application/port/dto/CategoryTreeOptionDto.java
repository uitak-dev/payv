package com.payv.ledger.application.port.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CategoryTreeOptionDto {
    private final String categoryId;
    private final String name;
    private final List<CategoryChildOptionDto> children;
}
