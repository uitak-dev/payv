package com.payv.iam.application.command;

import com.payv.iam.application.command.model.SignUpCommand;
import com.payv.iam.domain.model.User;
import com.payv.iam.domain.model.UserId;
import com.payv.iam.domain.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class IamCommandServiceTest {

    private InMemoryUserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private IamCommandService service;

    @Before
    public void setUp() {
        userRepository = new InMemoryUserRepository();
        passwordEncoder = new PrefixPasswordEncoder();
        service = new IamCommandService(userRepository, passwordEncoder);
    }

    @Test
    public void signUp_createsUser_withEncodedPassword() {
        SignUpCommand command = new SignUpCommand("test@example.com", "pw1234", "tester");

        UserId userId = service.signUp(command);

        User saved = userRepository.findById(userId).orElse(null);
        assertNotNull(saved);
        assertEquals("test@example.com", saved.getEmail());
        assertEquals("tester", saved.getDisplayName());
        assertTrue(saved.isActive());
        assertNotEquals("pw1234", saved.getPasswordHash());
        assertTrue(passwordEncoder.matches("pw1234", saved.getPasswordHash()));
    }

    @Test(expected = IllegalStateException.class)
    public void signUp_rejectsDuplicateEmail() {
        service.signUp(new SignUpCommand("dup@example.com", "pw1", "u1"));
        service.signUp(new SignUpCommand("dup@example.com", "pw2", "u2"));
    }

    private static class InMemoryUserRepository implements UserRepository {

        private final Map<UserId, User> storeById = new LinkedHashMap<>();

        @Override
        public void save(User user) {
            storeById.put(user.getId(), user);
        }

        @Override
        public Optional<User> findById(UserId userId) {
            return Optional.ofNullable(storeById.get(userId));
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

    private static class PrefixPasswordEncoder implements PasswordEncoder {

        @Override
        public String encode(CharSequence rawPassword) {
            return "ENC:" + rawPassword;
        }

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            return encode(rawPassword).equals(encodedPassword);
        }
    }
}
