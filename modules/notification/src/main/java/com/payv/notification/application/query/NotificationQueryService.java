package com.payv.notification.application.query;

import com.payv.common.application.query.PageRequest;
import com.payv.common.application.query.PagedResult;
import com.payv.notification.application.query.model.NotificationListItemView;
import com.payv.notification.domain.model.Notification;
import com.payv.notification.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
/**
 * 알림 조회 서비스.
 * - 알림 목록 페이지 조회와 미읽음 개수 조회를 제공한다.
 * - 페이징 규칙과 뷰 모델 변환을 서비스 계층에 모아, presentation 계층을 단순화 했다.
 */
public class NotificationQueryService {

    private final NotificationRepository notificationRepository;

    /**
     * 알림 목록을 페이지 단위로 조회한다.
     *
     * @param ownerUserId 소유 사용자 ID
     * @param page 페이지 번호(1-base)
     * @param size 페이지 크기
     * @return 페이징된 알림 목록 뷰
     */
    public PagedResult<NotificationListItemView> list(String ownerUserId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);

        List<Notification> notifications = notificationRepository.findListByOwner(
                ownerUserId,
                pageRequest.getOffset(),
                pageRequest.getSize()
        );
        int total = notificationRepository.countByOwner(ownerUserId);

        List<NotificationListItemView> items = new ArrayList<>(notifications.size());
        for (Notification notification : notifications) {
            items.add(new NotificationListItemView(
                    notification.getId().getValue(),
                    notification.getType(),
                    notification.getTitle(),
                    notification.getMessage(),
                    notification.getReferenceType(),
                    notification.getReferenceId(),
                    notification.isRead(),
                    notification.getCreatedAt(),
                    notification.getReadAt()
            ));
        }

        return new PagedResult<>(items, total, pageRequest.getPage(), pageRequest.getSize());
    }

    /**
     * 미읽음 알림 개수를 조회한다.
     *
     * @param ownerUserId 소유 사용자 ID
     * @return 미읽음 개수
     */
    public int unreadCount(String ownerUserId) {
        return notificationRepository.countUnreadByOwner(ownerUserId);
    }
}
