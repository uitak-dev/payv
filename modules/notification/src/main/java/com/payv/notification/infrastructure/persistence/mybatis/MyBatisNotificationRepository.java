package com.payv.notification.infrastructure.persistence.mybatis;

import com.payv.notification.domain.model.Notification;
import com.payv.notification.domain.model.NotificationId;
import com.payv.notification.domain.repository.NotificationRepository;
import com.payv.notification.infrastructure.persistence.mybatis.mapper.NotificationMapper;
import com.payv.notification.infrastructure.persistence.mybatis.record.NotificationRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class MyBatisNotificationRepository implements NotificationRepository {

    private final NotificationMapper notificationMapper;

    @Override
    public void save(Notification notification) {
        notificationMapper.upsert(NotificationRecord.toRecord(notification));
    }

    @Override
    public Optional<Notification> findById(NotificationId id, String ownerUserId) {
        NotificationRecord record = notificationMapper.selectByIdAndOwner(id.getValue(), ownerUserId);
        if (record == null) {
            return Optional.empty();
        }
        return Optional.of(record.toEntity());
    }

    @Override
    public List<Notification> findListByOwner(String ownerUserId, int offset, int limit) {
        List<NotificationRecord> records = notificationMapper.selectListByOwner(ownerUserId, offset, limit);
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }
        return records.stream().map(NotificationRecord::toEntity).collect(Collectors.toList());
    }

    @Override
    public int countByOwner(String ownerUserId) {
        return notificationMapper.countByOwner(ownerUserId);
    }

    @Override
    public int countUnreadByOwner(String ownerUserId) {
        return notificationMapper.countUnreadByOwner(ownerUserId);
    }

    @Override
    public int markAllAsRead(String ownerUserId) {
        return notificationMapper.markAllAsRead(ownerUserId);
    }
}
