package com.payv.notification.infrastructure.persistence.mybatis.mapper;

import com.payv.notification.infrastructure.persistence.mybatis.record.NotificationDispatchLogRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NotificationDispatchLogMapper {

    int insertIgnore(NotificationDispatchLogRecord record);
}
