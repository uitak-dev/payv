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
/**
 * 알림 쓰기 명령 서비스.
 * - 인앱 알림 생성, 단건 읽음 처리, 전체 읽음 처리를 수행한다.
 * - 알림 상태 변경을 한 경로로 통합해 읽음 상태 정합성을 유지한다.
 */
public class NotificationCommandService {

    private final NotificationRepository notificationRepository;

    /**
     * 인앱 알림을 생성한다.
     *
     * @param ownerUserId 알림 수신 사용자 ID
     * @param type 알림 유형
     * @param title 알림 제목
     * @param message 알림 본문
     * @param referenceType 참조 리소스 타입(예: BUDGET, TRANSACTION)
     * @param referenceId 참조 리소스 ID
     * @return 생성된 알림 ID
     */
    public NotificationId createInApp(String ownerUserId, NotificationType type,
                                      String title, String message,
                                      String referenceType, String referenceId) {

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

    /**
     * 알림 단건을 읽음 처리한다.
     *
     * @param notificationId 읽음 처리할 알림 ID
     * @param ownerUserId 소유 사용자 ID
     * @throws NotificationNotFoundException 알림이 없거나 소유자가 다른 경우
     */
    public void markRead(NotificationId notificationId, String ownerUserId) {
        Notification notification = notificationRepository.findById(notificationId, ownerUserId)
                .orElseThrow(NotificationNotFoundException::new);

        notification.ensureBelongsTo(ownerUserId);
        notification.markAsRead(OffsetDateTime.now());

        notificationRepository.save(notification);
    }

    /**
     * 사용자의 전체 알림을 읽음 처리한다.
     *
     * @param ownerUserId 소유 사용자 ID
     * @return 읽음 처리된 알림 건수
     */
    public int markAllRead(String ownerUserId) {
        return notificationRepository.markAllAsRead(ownerUserId);
    }
}
