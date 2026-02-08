package com.payv.iam.infrastructure.security;

import com.payv.iam.domain.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class IamUserDetails implements UserDetails {

    private final String userId;
    private final String email;
    private final String passwordHash;
    private final boolean active;

    private IamUserDetails(String userId, String email, String passwordHash, boolean active) {
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.active = active;
    }

    public static IamUserDetails from(User user) {
        return new IamUserDetails(
                user.getId().getValue(),
                user.getEmail(),
                user.getPasswordHash(),
                user.isActive()
        );
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils.createAuthorityList("ROLE_USER");
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return userId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
