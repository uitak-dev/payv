package com.payv.iam.presentation.dto.request;

import com.payv.iam.application.command.model.SignUpCommand;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class SignUpRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    private String displayName;

    public SignUpCommand toCommand() {
        return new SignUpCommand(email, password, displayName);
    }
}
