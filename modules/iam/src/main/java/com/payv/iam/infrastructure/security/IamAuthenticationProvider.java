package com.payv.iam.infrastructure.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component("iamAuthenticationProvider")
@RequiredArgsConstructor
@Slf4j
public class IamAuthenticationProvider implements AuthenticationProvider {

    private final IamUserDetailsService iamUserDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String rawPassword = authentication.getCredentials() == null
                ? null
                : authentication.getCredentials().toString();

        UserDetails userDetails;
        try {
            userDetails = iamUserDetailsService.loadUserByUsername(email);
        } catch (UsernameNotFoundException e) {
            log.warn("Authentication failed - user not found. email={}", email);
            throw new BadCredentialsException("Invalid credentials", e);
        } catch (Exception e) {
            log.error("Authentication failed during user loading. email={}", email, e);
            throw new BadCredentialsException("Invalid credentials", e);
        }

        if (!userDetails.isEnabled()) {
            log.warn("Authentication failed - inactive user. email={}", email);
            throw new BadCredentialsException("Inactive user");
        }
        boolean matched = rawPassword != null && passwordEncoder.matches(rawPassword, userDetails.getPassword());
        if (!matched) {
            log.warn("Authentication failed - password mismatch. email={}", email);
            throw new BadCredentialsException("Invalid credentials");
        }

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
