package com.payv.iam.infrastructure.persistence.mybatis.mapper;

import com.payv.iam.infrastructure.persistence.mybatis.record.UserRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    int upsert(UserRecord record);

    UserRecord selectById(@Param("userId") String userId);

    UserRecord selectByEmail(@Param("email") String email);

    int countByEmail(@Param("email") String email);
}
