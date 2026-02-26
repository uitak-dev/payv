package com.payv.notification.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

@Getter
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class NotificationId {

    private final String value;

    public static NotificationId generate() {
        return new NotificationId(UUID.randomUUID().toString());
    }

    public static NotificationId of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("notificationId must not be blank");
        }
        return new NotificationId(value);
    }
}
