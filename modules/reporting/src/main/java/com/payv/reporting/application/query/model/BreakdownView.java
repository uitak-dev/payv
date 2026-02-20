package com.payv.reporting.application.query.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BreakdownView {

    private final String refId;
    private final String name;
    private final long amount;
    private final int usagePercent;
}
