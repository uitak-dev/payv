package com.payv.common.application.query;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PagedResult<T> {
    private final List<T> items;
    private final int total;
    private final int page;
    private final int size;
}
