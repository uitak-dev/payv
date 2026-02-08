package com.payv.iam.infrastructure.persistence.mybatis.record;

import com.payv.iam.domain.model.User;
import com.payv.iam.domain.model.UserId;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class UserRecord {

    private String userId;
    private String email;
    private String passwordHash;
    private String displayName;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    private UserRecord(String userId, String email, String passwordHash, String displayName,
                       boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static UserRecord toRecord(User user) {
        return UserRecord.builder()
                .userId(user.getId().getValue())
                .email(user.getEmail())
                .passwordHash(user.getPasswordHash())
                .displayName(user.getDisplayName())
                .isActive(user.isActive())
                .build();
    }

    public User toEntity() {
        return User.of(
                UserId.of(userId),
                email,
                passwordHash,
                displayName,
                isActive
        );
    }
}
