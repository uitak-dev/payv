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
 * - Spring Batch를 쓰지 않는 환경에서도 호출할 수 있도록, "순수 애플리케이션 서비스"로 계획 로직을 분리.
 */
@Service
@RequiredArgsConstructor
public class FixedExpenseExecutionPlanningService {

    private final FixedExpenseDefinitionRepository definitionRepository;
    private final FixedExpenseExecutionRepository executionRepository;

    /**
     * 실행일에 해당하는 고정비 실행 인스턴스를 계획(생성)한다.
     *
     * What:
     * - 실행일 조건에 맞는 활성 정의만 DB에서 조회한다.
     * - 동일 정의/일자 실행 인스턴스가 이미 있으면 건너뛴다(idempotent).
     * - 없으면 실행 인스턴스를 생성/저장한다.
     *
     * Why:
     * - 배치 재실행이나 장애 복구 시 중복 거래 생성을 방지하면서
     *   미생성 건은 보완할 수 있게 하기 위함이다.
     *
     * @param runDate 실행 기준일. {@code null}이면 오늘 날짜
     * @return 새로 계획된 실행 인스턴스 수
     */
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
