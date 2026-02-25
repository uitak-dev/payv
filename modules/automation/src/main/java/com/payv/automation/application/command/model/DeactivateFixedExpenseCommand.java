package com.payv.automation.application.command.model;

import com.payv.automation.domain.model.FixedExpenseDefinitionId;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DeactivateFixedExpenseCommand {

    private final FixedExpenseDefinitionId definitionId;
}
