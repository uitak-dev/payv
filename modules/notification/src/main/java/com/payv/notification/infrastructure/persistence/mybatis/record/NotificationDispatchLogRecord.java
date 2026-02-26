package com.payv.notification.infrastructure.persistence.mybatis.record;

import com.payv.notification.domain.model.NotificationDispatchLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDispatchLogRecord {

    private String dispatchKey;
    private String ownerUserId;
    private String notificationType;

    public static NotificationDispatchLogRecord toRecord(NotificationDispatchLog log) {
        return NotificationDispatchLogRecord.builder()
                .dispatchKey(log.getDispatchKey())
                .ownerUserId(log.getOwnerUserId())
                .notificationType(log.getNotificationType().name())
                .build();
    }
}
