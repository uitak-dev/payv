package com.payv.automation.application.batch.job;

import com.payv.automation.application.port.LedgerTransactionPort;
import com.payv.automation.domain.model.FixedExpenseDefinition;
import com.payv.automation.domain.model.FixedExpenseExecution;
import com.payv.automation.domain.repository.FixedExpenseExecutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;

import java.time.LocalDateTime;
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
@Slf4j
public class FixedExpenseExecutionItemWriter implements ItemWriter<FixedExpenseExecutionPlanItem> {

    private final FixedExpenseExecutionRepository executionRepository;
    private final LedgerTransactionPort ledgerTransactionPort;

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
            try {
                String transactionId = ledgerTransactionPort.createFixedExpenseAutoTransaction(
                        execution,
                        definition.getOwnerUserId()
                );
                execution.markSucceeded(transactionId, null, LocalDateTime.now());
            } catch (Exception e) {
                log.error("Failed to create fixed expense auto transaction: definitionId={}, runDate={}",
                        definition.getId().getValue(), runDate, e);
                execution.markFailed(trimFailureReason(e), null, LocalDateTime.now());
            }
            executionRepository.save(execution);
        }
    }

    private String trimFailureReason(Exception e) {
        String message = e == null ? null : e.getMessage();
        if (message == null || message.trim().isEmpty()) {
            return "failed to create auto transaction";
        }
        String normalized = message.trim();
        return normalized.length() > 1000 ? normalized.substring(0, 1000) : normalized;
    }
}
