package com.payv.notification.application.command;

import com.payv.notification.application.exception.NotificationNotFoundException;
import com.payv.notification.domain.model.Notification;
import com.payv.notification.domain.model.NotificationId;
import com.payv.notification.domain.model.NotificationType;
import com.payv.notification.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationCommandService {

    private final NotificationRepository notificationRepository;

    public NotificationId createInApp(String ownerUserId,
                                      NotificationType type,
                                      String title,
                                      String message,
                                      String referenceType,
                                      String referenceId) {
        Notification notification = Notification.create(
                ownerUserId,
                type,
                title,
                message,
                referenceType,
                referenceId
        );
        notificationRepository.save(notification);
        return notification.getId();
    }

    public void markRead(NotificationId notificationId, String ownerUserId) {
        Notification notification = notificationRepository.findById(notificationId, ownerUserId)
                .orElseThrow(NotificationNotFoundException::new);

        notification.ensureBelongsTo(ownerUserId);
        notification.markAsRead(OffsetDateTime.now());

        notificationRepository.save(notification);
    }

    public int markAllRead(String ownerUserId) {
        return notificationRepository.markAllAsRead(ownerUserId);
    }
}
