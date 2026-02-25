package com.payv.automation.application.batch.job;

import com.payv.automation.domain.model.FixedExpenseDefinition;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

/**
 * Batch processor -> writer 사이에서 전달하는 처리 단위 객체.
 * definition 원본과 runDate를 같이 들고 다녀 writer에서 판단할 수 있게 한다.
 */
@Getter
@AllArgsConstructor
public final class FixedExpenseExecutionPlanItem {

    private final FixedExpenseDefinition definition;
    private final LocalDate runDate;
}
