package com.payv.automation.application.batch;

import com.payv.automation.domain.model.FixedExpenseDefinition;
import com.payv.automation.domain.model.FixedExpenseExecution;
import com.payv.automation.domain.repository.FixedExpenseDefinitionRepository;
import com.payv.automation.domain.repository.FixedExpenseExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 고정비 실행 계획 생성 서비스.
 *
 * Spring Batch를 쓰지 않는 환경에서도 호출할 수 있도록
 * "순수 애플리케이션 서비스"로 계획 로직을 분리.
 */
@Service
@RequiredArgsConstructor
public class FixedExpenseExecutionPlanningService {

    private final FixedExpenseDefinitionRepository definitionRepository;
    private final FixedExpenseExecutionRepository executionRepository;

    @Transactional
    public int planDueExecutions(LocalDate runDate) {
        LocalDate baseDate = runDate == null ? LocalDate.now() : runDate;

        // DB에서 runDate 실행 대상만 필터링해서 가져온다.
        List<FixedExpenseDefinition> scheduledDefinitions = definitionRepository.findAllActiveScheduledOn(baseDate);
        int plannedCount = 0;

        for (FixedExpenseDefinition definition : scheduledDefinitions) {
            boolean exists = executionRepository.existsByDefinitionAndScheduledDate(
                    definition.getOwnerUserId(), definition.getId(), baseDate
            );
            if (exists) {
                continue;
            }
            FixedExpenseExecution execution = definition.planExecution(baseDate);
            executionRepository.save(execution);
            plannedCount++;
        }

        return plannedCount;
    }
}
