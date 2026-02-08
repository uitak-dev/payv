package com.payv.iam.domain.repository;

import com.payv.iam.domain.model.User;
import com.payv.iam.domain.model.UserId;

import java.util.Optional;

public interface UserRepository {

    void save(User user);

    Optional<User> findById(UserId userId);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
