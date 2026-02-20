package com.payv.reporting.application.query.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TagSummaryView {

    private final String tagId;
    private final String name;
    private final long amount;
}
