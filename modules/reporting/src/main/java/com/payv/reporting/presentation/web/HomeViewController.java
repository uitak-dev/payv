package com.payv.reporting.presentation.web;

import com.payv.iam.infrastructure.security.IamUserDetails;
import com.payv.reporting.application.query.ReportingQueryService;
import com.payv.reporting.application.query.model.HomeDashboardView;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeViewController {

    private final ReportingQueryService reportingQueryService;

    @GetMapping({"/", "/home"})
    public String home(@AuthenticationPrincipal IamUserDetails userDetails, Model model) {
        String ownerUserId = userDetails.getUserId();

        HomeDashboardView dashboard = reportingQueryService.getHomeDashboard(ownerUserId, null);

        model.addAttribute("month", dashboard.getMonth());
        model.addAttribute("monthStart", dashboard.getMonthStart());
        model.addAttribute("monthEnd", dashboard.getMonthEnd());
        model.addAttribute("incomeAmount", dashboard.getIncomeAmount());
        model.addAttribute("expenseAmount", dashboard.getExpenseAmount());
        model.addAttribute("netAmount", dashboard.getNetAmount());
        model.addAttribute("recentTransactions", dashboard.getRecentTransactions());

        model.addAttribute("hasOverallBudget", dashboard.isHasOverallBudget());
        model.addAttribute("remainingBudget", dashboard.getRemainingBudget());
        model.addAttribute("budgetUsageRate", dashboard.getBudgetUsageRate());

        return "home/index";
    }
}
