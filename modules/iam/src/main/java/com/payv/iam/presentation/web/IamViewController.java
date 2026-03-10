package com.payv.iam.presentation.web;

import com.payv.iam.infrastructure.security.IamUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@Controller
@RequiredArgsConstructor
public class IamViewController {

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error,
                        @RequestParam(required = false) String logout,
                        @RequestParam(required = false) String success,
                        @RequestParam(required = false) String signupSuccess,
                        @AuthenticationPrincipal IamUserDetails userDetails,
                        Model model) {

        model.addAttribute("error", error);
        model.addAttribute("logout", logout);
        model.addAttribute("success", success);
        model.addAttribute("signupSuccess", signupSuccess);
        model.addAttribute("authenticatedUserId", userDetails == null ? null : userDetails.getUserId());
        return "iam/login";
    }

    @GetMapping("/signup")
    public String signUpForm(@RequestParam(required = false) String error,
                             @AuthenticationPrincipal IamUserDetails userDetails,
                             Model model) {
        if (userDetails != null) return "redirect:/ledger/transactions";
        model.addAttribute("error", error);
        return "iam/signup";
    }

    @GetMapping("/settings")
    public String settings(@AuthenticationPrincipal IamUserDetails userDetails, Model model) {
        model.addAttribute("email", userDetails == null ? null : userDetails.getEmail());
        model.addAttribute("currentMonth", YearMonth.now());
        return "iam/settings";
    }
}
