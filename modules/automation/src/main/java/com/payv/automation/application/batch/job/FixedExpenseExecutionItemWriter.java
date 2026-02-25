package com.payv.automation.application.batch.job;

import com.payv.automation.domain.model.FixedExpenseDefinition;
import com.payv.automation.domain.model.FixedExpenseExecution;
import com.payv.automation.domain.repository.FixedExpenseExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemWriter;

import java.time.LocalDate;
import java.util.List;

/**
 * 고정비 계획 생성 배치의 핵심 Writer.
 *
 * definition 1건을 받아:
 * - runDate가 해당 definition 실행일인지 확인하고
 * - 중복 생성은 exists 체크로 막고
 * - 필요한 경우 execution 1건을 생성한다.
 */
@RequiredArgsConstructor
public class FixedExpenseExecutionItemWriter implements ItemWriter<FixedExpenseExecutionPlanItem> {

    private final FixedExpenseExecutionRepository executionRepository;

    @Override
    public void write(List<? extends FixedExpenseExecutionPlanItem> items) {
        for (FixedExpenseExecutionPlanItem item : items) {
            FixedExpenseDefinition definition = item.getDefinition();
            LocalDate runDate = item.getRunDate();

            if (!definition.isScheduledOn(runDate)) {
                continue;
            }

            boolean exists = executionRepository.existsByDefinitionAndScheduledDate(
                    definition.getOwnerUserId(), definition.getId(), runDate
            );
            if (exists) {
                continue;
            }

            FixedExpenseExecution execution = definition.planExecution(runDate);
            executionRepository.save(execution);
        }
    }
}
