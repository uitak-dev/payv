package com.payv.iam.presentation.web;

import com.payv.iam.application.command.IamCommandService;
import com.payv.iam.application.command.model.SignUpCommand;
import com.payv.common.presentation.api.AjaxResponses;
import com.payv.iam.infrastructure.security.IamUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/signup")
@RequiredArgsConstructor
public class IamSignUpViewController {

    private final IamCommandService commandService;

    @GetMapping
    public String signUpForm(@RequestParam(required = false) String error,
                             @AuthenticationPrincipal IamUserDetails userDetails,
                             Model model) {
        if (userDetails != null) return "redirect:/ledger/transactions";
        model.addAttribute("error", error);
        return "iam/signup";
    }

    @PostMapping(produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> signUp(@RequestParam String email, @RequestParam String password,
                                                      @RequestParam(required = false) String displayName) {
        commandService.signUp(new SignUpCommand(email, password, displayName));
        return AjaxResponses.okRedirect("/login?signupSuccess=true");
    }
}
