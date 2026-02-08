package com.payv.iam.application.command.model;

import lombok.Getter;

@Getter
public class SignUpCommand {

    private final String email;
    private final String password;
    private final String displayName;

    public SignUpCommand(String email, String password, String displayName) {
        this.email = email;
        this.password = password;
        this.displayName = displayName;
    }
}
