package com.payv.iam.application.query.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserProfileView {
    private final String userId;
    private final String email;
    private final String displayName;
}
