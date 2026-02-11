package com.payv.iam.presentation.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.time.YearMonth;

@Controller
@RequestMapping("/settings")
public class IamSettingsViewController {

    @GetMapping
    public String settings(Principal principal, Model model) {
        model.addAttribute("ownerUserId", principal == null ? null : principal.getName());
        model.addAttribute("currentMonth", YearMonth.now());
        return "iam/settings";
    }
}
