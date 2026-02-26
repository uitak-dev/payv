package com.payv.notification.infrastructure.persistence.mybatis.record;

import com.payv.notification.domain.model.Notification;
import com.payv.notification.domain.model.NotificationId;
import com.payv.notification.domain.model.NotificationType;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
public class NotificationRecord {

    private String notificationId;
    private String ownerUserId;
    private String notificationType;
    private String title;
    private String message;
    private String referenceType;
    private String referenceId;
    private boolean isRead;
    private OffsetDateTime readAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @Builder
    private NotificationRecord(String notificationId,
                               String ownerUserId,
                               String notificationType,
                               String title,
                               String message,
                               String referenceType,
                               String referenceId,
                               boolean isRead,
                               OffsetDateTime readAt,
                               OffsetDateTime createdAt,
                               OffsetDateTime updatedAt) {
        this.notificationId = notificationId;
        this.ownerUserId = ownerUserId;
        this.notificationType = notificationType;
        this.title = title;
        this.message = message;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.isRead = isRead;
        this.readAt = readAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static NotificationRecord toRecord(Notification notification) {
        return NotificationRecord.builder()
                .notificationId(notification.getId().getValue())
                .ownerUserId(notification.getOwnerUserId())
                .notificationType(notification.getType().name())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .referenceType(notification.getReferenceType())
                .referenceId(notification.getReferenceId())
                .isRead(notification.isRead())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }

    public Notification toEntity() {
        return Notification.of(
                NotificationId.of(notificationId),
                ownerUserId,
                NotificationType.valueOf(notificationType),
                title,
                message,
                referenceType,
                referenceId,
                Boolean.TRUE.equals(isRead),
                readAt,
                createdAt,
                updatedAt
        );
    }
}
