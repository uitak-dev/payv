package com.payv.budget.presentation.dto.request;

import lombok.Data;

@Data
public final class BudgetListNoticeRequest {
    private String created;
    private String updated;
    private String deactivated;
    private String error;
}
