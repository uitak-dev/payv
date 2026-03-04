package com.payv.reporting.application.query.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
public class BreakdownView implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String refId;
    private final String name;
    private final long amount;
    private final int usagePercent;
}
