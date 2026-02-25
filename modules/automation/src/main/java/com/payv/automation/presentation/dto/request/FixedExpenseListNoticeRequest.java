package com.payv.automation.presentation.dto.request;

import lombok.Data;

@Data
public class FixedExpenseListNoticeRequest {

    private String created;
    private String updated;
    private String deactivated;
    private String error;
}
