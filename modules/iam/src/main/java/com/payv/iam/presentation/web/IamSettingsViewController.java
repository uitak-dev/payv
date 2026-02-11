package com.payv.iam.presentation.web;

import com.payv.iam.infrastructure.security.IamUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.YearMonth;

@Controller
@RequestMapping("/settings")
public class IamSettingsViewController {

    @GetMapping
    public String settings(@AuthenticationPrincipal IamUserDetails userDetails, Model model) {
        model.addAttribute("email", userDetails == null ? null : userDetails.getEmail());
        model.addAttribute("currentMonth", YearMonth.now());
        return "iam/settings";
    }
}
