package com.payv.iam.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.util.Locale;
import java.util.Objects;

@Getter
public class User {

    private static final int MAX_EMAIL_LENGTH = 200;
    private static final int MAX_DISPLAY_NAME_LENGTH = 50;

    private UserId id;
    private String email;
    private String passwordHash;
    private String displayName;
    private boolean isActive;

    @Builder
    private User(UserId id, String email, String passwordHash, String displayName, boolean isActive) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.isActive = isActive;
    }

    public static User create(String email, String passwordHash, String displayName) {
        return User.builder()
                .id(UserId.generate())
                .email(normalizeEmail(email))
                .passwordHash(requirePasswordHash(passwordHash))
                .displayName(normalizeDisplayName(displayName))
                .isActive(true)
                .build();
    }

    public static User of(UserId id, String email, String passwordHash, String displayName, boolean isActive) {
        return User.builder()
                .id(id)
                .email(normalizeEmail(email))
                .passwordHash(requirePasswordHash(passwordHash))
                .displayName(normalizeDisplayName(displayName))
                .isActive(isActive)
                .build();
    }

    public void ensureActive() {
        if (!isActive) {
            throw new IllegalStateException("inactive user");
        }
    }

    public boolean matchesPasswordHash(String hashedPassword) {
        return Objects.equals(this.passwordHash, requirePasswordHash(hashedPassword));
    }

    public void deactivate() {
        this.isActive = false;
    }

    public static String normalizeEmail(String email) {
        String ret = (email == null) ? null : email.trim().toLowerCase(Locale.ROOT);
        if (ret == null || ret.isEmpty()) {
            throw new IllegalArgumentException("email must not be blank");
        }
        if (ret.length() > MAX_EMAIL_LENGTH) {
            throw new IllegalArgumentException("email length must be <= " + MAX_EMAIL_LENGTH);
        }
        return ret;
    }

    private static String requirePasswordHash(String passwordHash) {
        String ret = (passwordHash == null) ? null : passwordHash.trim();
        if (ret == null || ret.isEmpty()) {
            throw new IllegalArgumentException("passwordHash must not be blank");
        }
        return ret;
    }

    public static String normalizeDisplayName(String displayName) {
        String ret = (displayName == null) ? "" : displayName.trim();
        if (ret.length() > MAX_DISPLAY_NAME_LENGTH) {
            throw new IllegalArgumentException("displayName length must be <= " + MAX_DISPLAY_NAME_LENGTH);
        }
        return ret;
    }
}
