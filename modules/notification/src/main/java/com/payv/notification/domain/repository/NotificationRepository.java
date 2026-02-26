package com.payv.notification.domain.repository;

import com.payv.notification.domain.model.Notification;
import com.payv.notification.domain.model.NotificationId;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository {

    void save(Notification notification);

    Optional<Notification> findById(NotificationId id, String ownerUserId);

    List<Notification> findListByOwner(String ownerUserId, int offset, int limit);

    int countByOwner(String ownerUserId);

    int countUnreadByOwner(String ownerUserId);

    int markAllAsRead(String ownerUserId);
}
