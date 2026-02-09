package com.payv.iam.application.command;

import com.payv.iam.application.command.model.SignUpCommand;
import com.payv.iam.domain.model.User;
import com.payv.iam.domain.model.UserId;
import com.payv.iam.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class IamCommandService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserId signUp(SignUpCommand command) {
        Objects.requireNonNull(command, "command");

        String normalizedEmail = User.normalizeEmail(command.getEmail());
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalStateException("email already exists");
        }

        User user = User.create(
                normalizedEmail,
                passwordEncoder.encode(requireRawPassword(command.getPassword())),
                command.getDisplayName()
        );

        userRepository.save(user);
        return user.getId();
    }

    private static String requireRawPassword(String rawPassword) {
        String ret = (rawPassword == null) ? null : rawPassword.trim();
        if (ret == null || ret.isEmpty()) {
            throw new IllegalArgumentException("password must not be blank");
        }
        return ret;
    }
}
