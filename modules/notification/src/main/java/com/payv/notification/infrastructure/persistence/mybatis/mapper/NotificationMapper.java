package com.payv.notification.infrastructure.persistence.mybatis.mapper;

import com.payv.notification.infrastructure.persistence.mybatis.record.NotificationRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationMapper {

    int upsert(NotificationRecord record);

    NotificationRecord selectByIdAndOwner(@Param("notificationId") String notificationId,
                                          @Param("ownerUserId") String ownerUserId);

    List<NotificationRecord> selectListByOwner(@Param("ownerUserId") String ownerUserId,
                                               @Param("offset") int offset,
                                               @Param("limit") int limit);

    int countByOwner(@Param("ownerUserId") String ownerUserId);

    int countUnreadByOwner(@Param("ownerUserId") String ownerUserId);

    int markAllAsRead(@Param("ownerUserId") String ownerUserId);
}
