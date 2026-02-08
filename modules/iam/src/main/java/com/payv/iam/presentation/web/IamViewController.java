package com.payv.iam.presentation.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
@RequestMapping("/login")
public class IamViewController {

    @GetMapping
    public String login(@RequestParam(required = false) String error,
                        @RequestParam(required = false) String logout,
                        @RequestParam(required = false) String success,
                        Principal principal,
                        Model model) {

        model.addAttribute("error", error);
        model.addAttribute("logout", logout);
        model.addAttribute("success", success);
        model.addAttribute("authenticatedUserId", principal == null ? null : principal.getName());
        return "iam/login";
    }
}
