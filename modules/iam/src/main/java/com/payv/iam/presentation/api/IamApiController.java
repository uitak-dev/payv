package com.payv.iam.presentation.api;

import com.payv.iam.application.command.IamCommandService;
import com.payv.iam.application.command.model.SignUpCommand;
import com.payv.iam.application.query.IamQueryService;
import com.payv.iam.domain.model.UserId;
import com.payv.iam.presentation.dto.request.LoginRequest;
import com.payv.iam.presentation.dto.request.SignUpRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/iam/auth")
public class IamApiController {

    private final IamCommandService commandService;
    private final IamQueryService queryService;
    private final AuthenticationManager authenticationManager;

    public IamApiController(IamCommandService commandService,
                            IamQueryService queryService,
                            @Qualifier("authenticationManager") AuthenticationManager authenticationManager) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signUp(@RequestBody SignUpRequest req) {
        UserId userId = commandService.signUp(new SignUpCommand(
                req.getEmail(),
                req.getPassword(),
                req.getDisplayName()
        ));

        return ResponseEntity.status(201).body(new SignUpResponse(userId.getValue()));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req,
                                               HttpServletRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
            );
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).build();
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        request.getSession(true).setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        return ResponseEntity.ok(new LoginResponse(authentication.getName()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(401).build();
        }

        String userId = authentication.getName();
        IamQueryService.UserProfileView view = queryService.getUserProfile(userId)
                .orElseThrow(() -> new IllegalStateException("authenticated user not found"));

        return ResponseEntity.ok(new MeResponse(
                view.getUserId(),
                view.getEmail(),
                view.getDisplayName()
        ));
    }

    @Data
    @AllArgsConstructor
    public static class SignUpResponse {
        private final String userId;
    }

    @Data
    @AllArgsConstructor
    public static class LoginResponse {
        private final String userId;
    }

    @Data
    @AllArgsConstructor
    public static class MeResponse {
        private final String userId;
        private final String email;
        private final String displayName;
    }
}
