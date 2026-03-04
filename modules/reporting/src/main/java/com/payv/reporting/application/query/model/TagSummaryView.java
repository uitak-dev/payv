package com.payv.reporting.application.query.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
public class TagSummaryView implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String tagId;
    private final String name;
    private final long amount;
}
