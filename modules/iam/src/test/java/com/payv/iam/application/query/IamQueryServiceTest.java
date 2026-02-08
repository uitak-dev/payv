package com.payv.iam.application.query;

import com.payv.iam.domain.model.User;
import com.payv.iam.domain.model.UserId;
import com.payv.iam.domain.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IamQueryServiceTest {

    private InMemoryUserRepository userRepository;
    private IamQueryService service;

    @Before
    public void setUp() {
        userRepository = new InMemoryUserRepository();
        service = new IamQueryService(userRepository);
    }

    @Test
    public void getUserProfile_returnsProfileWhenUserExists() {
        User user = User.create("me@example.com", "hashed", "me");
        userRepository.save(user);

        Optional<IamQueryService.UserProfileView> result = service.getUserProfile(user.getId().getValue());

        assertTrue(result.isPresent());
        assertEquals(user.getId().getValue(), result.get().getUserId());
        assertEquals("me@example.com", result.get().getEmail());
    }

    @Test
    public void getUserProfile_returnsEmptyWhenIdBlank() {
        Optional<IamQueryService.UserProfileView> result = service.getUserProfile(" ");
        assertFalse(result.isPresent());
    }

    @Test(expected = IllegalStateException.class)
    public void validateOwnership_throwsWhenOwnerMismatch() {
        service.validateOwnership("user-1", "user-2");
    }

    private static class InMemoryUserRepository implements UserRepository {

        private final Map<UserId, User> storeById = new LinkedHashMap<>();

        @Override
        public void save(User user) {
            storeById.put(user.getId(), user);
        }

        @Override
        public Optional<User> findById(UserId userId) {
            User user = storeById.get(userId);
            if (user == null || !user.isActive()) {
                return Optional.empty();
            }
            return Optional.of(user);
        }

        @Override
        public Optional<User> findByEmail(String email) {
            for (User user : storeById.values()) {
                if (user.getEmail().equals(email) && user.isActive()) {
                    return Optional.of(user);
                }
            }
            return Optional.empty();
        }

        @Override
        public boolean existsByEmail(String email) {
            return findByEmail(email).isPresent();
        }
    }
}
