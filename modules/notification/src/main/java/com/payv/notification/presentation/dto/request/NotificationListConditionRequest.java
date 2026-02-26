package com.payv.notification.presentation.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationListConditionRequest {

    private int page = 1;
    private int size = 20;
}
