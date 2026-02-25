package com.payv.automation.presentation.dto.request;

import com.payv.automation.application.command.model.UpdateFixedExpenseCommand;
import com.payv.automation.domain.model.FixedExpenseDefinitionId;
import lombok.Data;

@Data
public class UpdateFixedExpenseRequest {

    private String name;
    private Long amount;
    private String assetId;
    private String categoryIdLevel1;
    private String categoryIdLevel2;
    private String memo;
    private Integer dayOfMonth;
    private String scheduleType;

    public UpdateFixedExpenseCommand toCommand(String definitionId) {
        boolean endOfMonth = "EOM".equalsIgnoreCase(scheduleType);
        return new UpdateFixedExpenseCommand(
                FixedExpenseDefinitionId.of(definitionId),
                name,
                amount == null ? 0L : amount,
                assetId,
                categoryIdLevel1,
                normalizeNullable(categoryIdLevel2),
                memo,
                endOfMonth ? null : dayOfMonth,
                endOfMonth
        );
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
