package com.payv.reporting.infrastructure.persistence.mybatis.record;

import lombok.Getter;

@Getter
public class AmountByIdRecord {
    private String refId;
    private long amount;
}
