package com.payv.notification.domain.repository;

import com.payv.notification.domain.model.NotificationDispatchLog;

public interface NotificationDispatchLogRepository {

    boolean appendIfAbsent(NotificationDispatchLog log);
}
