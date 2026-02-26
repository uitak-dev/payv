package com.payv.notification.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
public final class NotificationDispatchLog {

    private final String dispatchKey;
    private final String ownerUserId;
    private final NotificationType notificationType;

    @Builder
    private NotificationDispatchLog(String dispatchKey, String ownerUserId,
                                    NotificationType notificationType) {
        this.dispatchKey = requireText(dispatchKey, "dispatchKey");
        this.ownerUserId = requireText(ownerUserId, "ownerUserId");
        if (notificationType == null) {
            throw new IllegalArgumentException("notificationType must not be null");
        }
        this.notificationType = notificationType;
    }

    public static NotificationDispatchLog of(String dispatchKey,
                                             String ownerUserId,
                                             NotificationType type) {
        return NotificationDispatchLog.builder()
                .dispatchKey(dispatchKey)
                .ownerUserId(ownerUserId)
                .notificationType(type)
                .build();
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }
}
