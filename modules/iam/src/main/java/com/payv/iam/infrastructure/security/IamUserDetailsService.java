package com.payv.iam.infrastructure.security;

import com.payv.iam.domain.model.User;
import com.payv.iam.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component("iamUserDetailsService")
@RequiredArgsConstructor
public class IamUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            String normalizedEmail = User.normalizeEmail(username);
            User user = userRepository.findByEmail(normalizedEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("user not found"));
            return IamUserDetails.from(user);
        } catch (IllegalArgumentException e) {
            throw new UsernameNotFoundException("user not found", e);
        }
    }
}
