package com.payv.automation.application.batch.job;

import com.payv.automation.domain.model.FixedExpenseDefinition;
import org.springframework.batch.item.ItemProcessor;

import java.time.LocalDate;

/**
 * Reader에서 받은 definition에 runDate 컨텍스트를 결합한다.
 *
 * 실제 비즈니스 처리는 Writer에서 수행해,
 * 한 곳에서 idempotency를 일관되게 관리한다.
 */
public class FixedExpenseExecutionItemProcessor implements ItemProcessor<FixedExpenseDefinition, FixedExpenseExecutionPlanItem> {

    private final LocalDate runDate;

    public FixedExpenseExecutionItemProcessor(LocalDate runDate) {
        this.runDate = runDate;
    }

    @Override
    public FixedExpenseExecutionPlanItem process(FixedExpenseDefinition definition) {
        return new FixedExpenseExecutionPlanItem(definition, runDate);
    }
}
