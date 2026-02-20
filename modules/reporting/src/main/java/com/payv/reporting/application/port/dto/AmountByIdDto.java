package com.payv.reporting.application.port.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AmountByIdDto {
    private final String refId;
    private final long amount;
}
