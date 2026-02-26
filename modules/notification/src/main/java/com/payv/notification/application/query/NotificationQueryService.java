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
public class NotificationQueryService {

    private final NotificationRepository notificationRepository;

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

    public int unreadCount(String ownerUserId) {
        return notificationRepository.countUnreadByOwner(ownerUserId);
    }
}
