package com.payv.ledger.application.port.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategoryChildOptionDto {
    private final String categoryId;
    private final String name;
}
