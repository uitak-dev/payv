package com.payv.reporting.presentation.web;

import com.payv.budget.application.query.BudgetQueryService;
import com.payv.ledger.application.query.TransactionQueryService;
import com.payv.ledger.presentation.dto.viewmodel.TransactionSummaryView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeViewController {

    private final BudgetQueryService budgetQueryService;
    private final TransactionQueryService transactionQueryService;

    @GetMapping({"/", "/home"})
    public String home(Principal principal, Model model) {
        String ownerUserId = principal.getName();

        YearMonth currentMonth = YearMonth.now();
        LocalDate monthStart = currentMonth.atDay(1);
        LocalDate monthEnd = currentMonth.atEndOfMonth();

        List<BudgetQueryService.BudgetView> monthlyBudgets = budgetQueryService.getMonthlyBudgets(ownerUserId, currentMonth);
        BudgetQueryService.BudgetView overallBudget = monthlyBudgets.stream()
                .filter(b -> b.getCategoryId() == null)
                .findFirst()
                .orElse(null);

        long incomeAmount = transactionQueryService.sumAmountByType(ownerUserId, monthStart, monthEnd, "INCOME");
        long expenseAmount = transactionQueryService.sumAmountByType(ownerUserId, monthStart, monthEnd, "EXPENSE");

        TransactionQueryService.PagedResult<TransactionSummaryView> recent =
                transactionQueryService.list(ownerUserId, monthStart, monthEnd, null, 1, 20);

        model.addAttribute("month", currentMonth);
        model.addAttribute("monthStart", monthStart);
        model.addAttribute("monthEnd", monthEnd);
        model.addAttribute("incomeAmount", incomeAmount);
        model.addAttribute("expenseAmount", expenseAmount);
        model.addAttribute("netAmount", incomeAmount - expenseAmount);
        model.addAttribute("recentTransactions", recent == null ? Collections.emptyList() : recent.getItems());

        model.addAttribute("overallBudget", overallBudget);
        model.addAttribute("hasOverallBudget", overallBudget != null);
        model.addAttribute("remainingBudget", overallBudget == null ? 0L : overallBudget.getRemainingAmount());
        model.addAttribute("budgetUsageRate", overallBudget == null ? 0 : overallBudget.getUsageRate());

        return "home/index";
    }
}
