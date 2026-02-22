package com.payv.iam.application.query;

import com.payv.iam.application.exception.OwnershipDeniedException;
import com.payv.iam.domain.model.UserId;
import com.payv.iam.domain.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IamQueryService {

    private final UserRepository userRepository;

    public Optional<UserProfileView> getUserProfile(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return Optional.empty();
        }
        return userRepository.findById(UserId.of(userId))
                .map(user -> new UserProfileView(
                        user.getId().getValue(),
                        user.getEmail(),
                        user.getDisplayName()
                ));
    }

    public void validateOwnership(String requesterUserId, String resourceOwnerUserId) {
        if (!Objects.equals(requesterUserId, resourceOwnerUserId)) {
            throw new OwnershipDeniedException();
        }
    }

    @Getter
    @AllArgsConstructor
    public static class UserProfileView {
        private final String userId;
        private final String email;
        private final String displayName;
    }
}
