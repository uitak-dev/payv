package com.payv.notification.application.query.model;

import com.payv.notification.domain.model.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@AllArgsConstructor
public class NotificationListItemView {

    private final String notificationId;
    private final NotificationType type;
    private final String title;
    private final String message;
    private final String referenceType;
    private final String referenceId;
    private final boolean isRead;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime readAt;
}
