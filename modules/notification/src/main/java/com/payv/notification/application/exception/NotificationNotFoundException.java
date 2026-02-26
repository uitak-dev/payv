package com.payv.notification.application.exception;

import com.payv.common.error.NotFoundException;

public class NotificationNotFoundException extends NotFoundException {

    public NotificationNotFoundException() {
        super("NOTIFICATION-404", "notification not found");
    }
}
