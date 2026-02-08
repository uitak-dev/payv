package com.payv.iam.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component("iamAuthenticationProvider")
@RequiredArgsConstructor
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
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid credentials", e);
        }

        if (!userDetails.isEnabled()) {
            throw new BadCredentialsException("Inactive user");
        }
        if (rawPassword == null || !passwordEncoder.matches(rawPassword, userDetails.getPassword())) {
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
