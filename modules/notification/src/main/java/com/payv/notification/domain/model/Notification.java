package com.payv.notification.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Objects;

@Getter
public final class Notification {

    private final NotificationId id;
    private final String ownerUserId;
    private final NotificationType type;
    private final String title;
    private final String message;
    private final String referenceType;
    private final String referenceId;

    private boolean isRead;
    private OffsetDateTime readAt;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @Builder
    private Notification(NotificationId id,
                         String ownerUserId,
                         NotificationType type,
                         String title,
                         String message,
                         String referenceType,
                         String referenceId,
                         boolean isRead,
                         OffsetDateTime readAt,
                         OffsetDateTime createdAt,
                         OffsetDateTime updatedAt) {
        this.id = requireId(id);
        this.ownerUserId = requireOwner(ownerUserId);
        this.type = requireType(type);
        this.title = requireText(title, "title");
        this.message = requireText(message, "message");
        this.referenceType = normalizeNullable(referenceType);
        this.referenceId = normalizeNullable(referenceId);
        this.isRead = isRead;
        this.readAt = readAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Notification create(String ownerUserId,
                                      NotificationType type,
                                      String title,
                                      String message,
                                      String referenceType,
                                      String referenceId) {
        return Notification.builder()
                .id(NotificationId.generate())
                .ownerUserId(ownerUserId)
                .type(type)
                .title(title)
                .message(message)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .isRead(false)
                .build();
    }

    public static Notification of(NotificationId id,
                                  String ownerUserId,
                                  NotificationType type,
                                  String title,
                                  String message,
                                  String referenceType,
                                  String referenceId,
                                  boolean isRead,
                                  OffsetDateTime readAt,
                                  OffsetDateTime createdAt,
                                  OffsetDateTime updatedAt) {
        return Notification.builder()
                .id(id)
                .ownerUserId(ownerUserId)
                .type(type)
                .title(title)
                .message(message)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .isRead(isRead)
                .readAt(readAt)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public void markAsRead(OffsetDateTime when) {
        if (isRead) {
            return;
        }
        this.isRead = true;
        this.readAt = when == null ? OffsetDateTime.now() : when;
    }

    public void ensureBelongsTo(String requesterOwnerUserId) {
        if (!Objects.equals(this.ownerUserId, requesterOwnerUserId)) {
            throw new IllegalStateException("notification owner mismatch");
        }
    }

    private static NotificationId requireId(NotificationId id) {
        if (id == null) {
            throw new IllegalArgumentException("notificationId must not be null");
        }
        return id;
    }

    private static String requireOwner(String ownerUserId) {
        if (ownerUserId == null || ownerUserId.trim().isEmpty()) {
            throw new IllegalArgumentException("ownerUserId must not be blank");
        }
        return ownerUserId;
    }

    private static NotificationType requireType(NotificationType type) {
        if (type == null) {
            throw new IllegalArgumentException("notificationType must not be null");
        }
        return type;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    private static String normalizeNullable(String value) {
        if (value == null) return null;
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
