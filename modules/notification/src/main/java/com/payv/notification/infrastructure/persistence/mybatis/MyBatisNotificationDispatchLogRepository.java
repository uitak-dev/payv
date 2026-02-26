package com.payv.notification.infrastructure.persistence.mybatis;

import com.payv.notification.domain.model.NotificationDispatchLog;
import com.payv.notification.domain.repository.NotificationDispatchLogRepository;
import com.payv.notification.infrastructure.persistence.mybatis.mapper.NotificationDispatchLogMapper;
import com.payv.notification.infrastructure.persistence.mybatis.record.NotificationDispatchLogRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MyBatisNotificationDispatchLogRepository implements NotificationDispatchLogRepository {

    private final NotificationDispatchLogMapper mapper;

    @Override
    public boolean appendIfAbsent(NotificationDispatchLog log) {
        int inserted = mapper.insertIgnore(NotificationDispatchLogRecord.toRecord(log));
        return inserted > 0;
    }
}
