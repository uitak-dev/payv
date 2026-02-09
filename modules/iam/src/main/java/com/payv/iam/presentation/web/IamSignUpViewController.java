package com.payv.iam.presentation.web;

import com.payv.iam.application.command.IamCommandService;
import com.payv.iam.application.command.model.SignUpCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/signup")
@RequiredArgsConstructor
public class IamSignUpViewController {

    private final IamCommandService commandService;

    @GetMapping
    public String signUpForm(@RequestParam(required = false) String error,
                             Principal principal,
                             Model model) {
        if (principal != null) return "redirect:/ledger/transactions";
        model.addAttribute("error", error);
        return "iam/signup";
    }

    @PostMapping(produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> signUp(@RequestParam String email,
                                                       @RequestParam String password,
                                                       @RequestParam(required = false) String displayName) {
        try {
            commandService.signUp(new SignUpCommand(email, password, displayName));
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("success", true);
            body.put("redirectUrl", "/login?signupSuccess=true");
            return ResponseEntity.ok(body);
        } catch (RuntimeException e) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("success", false);
            body.put("message", e.getMessage() == null ? "signup failed" : e.getMessage());
            return ResponseEntity.badRequest().body(body);
        }
    }
}
