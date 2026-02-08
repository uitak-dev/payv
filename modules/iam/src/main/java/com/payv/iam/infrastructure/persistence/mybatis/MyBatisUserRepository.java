package com.payv.iam.infrastructure.persistence.mybatis;

import com.payv.iam.domain.model.User;
import com.payv.iam.domain.model.UserId;
import com.payv.iam.domain.repository.UserRepository;
import com.payv.iam.infrastructure.persistence.mybatis.mapper.UserMapper;
import com.payv.iam.infrastructure.persistence.mybatis.record.UserRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MyBatisUserRepository implements UserRepository {

    private final UserMapper userMapper;

    @Override
    public void save(User user) {
        userMapper.upsert(UserRecord.toRecord(user));
    }

    @Override
    public Optional<User> findById(UserId userId) {
        UserRecord record = userMapper.selectById(userId.getValue());
        if (record == null) return Optional.empty();
        return Optional.of(record.toEntity());
    }

    @Override
    public Optional<User> findByEmail(String email) {
        UserRecord record = userMapper.selectByEmail(email);
        if (record == null) return Optional.empty();
        return Optional.of(record.toEntity());
    }

    @Override
    public boolean existsByEmail(String email) {
        return userMapper.countByEmail(email) > 0;
    }
}
