package com.payv.budget.presentation.web;

import com.payv.budget.application.port.ClassificationQueryPort;
import com.payv.budget.application.query.BudgetQueryService;
import com.payv.budget.application.query.model.BudgetView;
import com.payv.budget.domain.model.BudgetId;
import com.payv.budget.presentation.dto.request.BudgetListConditionRequest;
import com.payv.budget.presentation.dto.request.BudgetListNoticeRequest;
import com.payv.iam.infrastructure.security.IamUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@Controller
@RequestMapping("/budget/budgets")
@RequiredArgsConstructor
public class BudgetViewController {

    private final BudgetQueryService queryService;
    private final ClassificationQueryPort classificationQueryPort;

    @GetMapping
    public String list(@AuthenticationPrincipal IamUserDetails userDetails,
                       @ModelAttribute("condition") BudgetListConditionRequest condition,
                       @ModelAttribute("notice") BudgetListNoticeRequest notice,
                       Model model) {
        String ownerUserId = userDetails.getUserId();
        YearMonth targetMonth = condition.resolvedMonth();

        model.addAttribute("selectedMonth", targetMonth.toString());
        model.addAttribute("budgets", queryService.getMonthlyBudgets(ownerUserId, targetMonth));
        model.addAttribute("categories", classificationQueryPort.getAllCategories(ownerUserId));
        model.addAttribute("notice", notice);
        return "budget/list";
    }

    @GetMapping("/new")
    public String createForm(@AuthenticationPrincipal IamUserDetails userDetails,
                             @RequestParam(required = false) String month,
                             @RequestParam(required = false) String error,
                             Model model) {
        String ownerUserId = userDetails.getUserId();
        YearMonth targetMonth = parseMonthOrNow(month);

        model.addAttribute("selectedMonth", targetMonth.toString());
        model.addAttribute("categories", classificationQueryPort.getAllCategories(ownerUserId));
        model.addAttribute("error", error);
        return "budget/create";
    }

    @GetMapping("/{budgetId}")
    public String detail(@AuthenticationPrincipal IamUserDetails userDetails,
                         @PathVariable String budgetId,
                         @RequestParam(required = false) String deactivated,
                         @RequestParam(required = false) String error,
                         Model model) {
        String ownerUserId = userDetails.getUserId();
        BudgetView budget = queryService.get(BudgetId.of(budgetId), ownerUserId)
                .orElseThrow(() -> new IllegalStateException("budget not found"));

        model.addAttribute("budget", budget);
        model.addAttribute("deactivated", deactivated);
        model.addAttribute("error", error);
        return "budget/detail";
    }

    @GetMapping("/{budgetId}/edit")
    public String editForm(@AuthenticationPrincipal IamUserDetails userDetails,
                           @PathVariable String budgetId,
                           @RequestParam(required = false) String error,
                           Model model) {
        String ownerUserId = userDetails.getUserId();
        BudgetView budget = queryService.get(BudgetId.of(budgetId), ownerUserId)
                .orElseThrow(() -> new IllegalStateException("budget not found"));

        model.addAttribute("budget", budget);
        model.addAttribute("categories", classificationQueryPort.getAllCategories(ownerUserId));
        model.addAttribute("error", error);
        return "budget/edit";
    }

    private YearMonth parseMonthOrNow(String month) {
        if (month == null || month.trim().isEmpty()) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(month.trim());
        } catch (RuntimeException e) {
            return YearMonth.now();
        }
    }
}
