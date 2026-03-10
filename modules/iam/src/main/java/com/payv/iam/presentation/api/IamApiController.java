package com.payv.iam.presentation.api;

import com.payv.common.presentation.api.AjaxResponses;
import com.payv.iam.application.command.IamCommandService;
import com.payv.iam.presentation.dto.request.SignUpRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/iam")
@RequiredArgsConstructor
public class IamApiController {

    private final IamCommandService commandService;

    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signUp(@ModelAttribute SignUpRequest request) {
        commandService.signUp(request.toCommand());
        return AjaxResponses.okRedirect("/login?signupSuccess=true");
    }
}
