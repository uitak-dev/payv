package com.payv.iam.presentation.web;

import com.payv.iam.infrastructure.security.IamUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/login")
public class IamViewController {

    @GetMapping
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
}
