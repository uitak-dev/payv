package com.payv.budget.presentation.web;

import com.payv.budget.application.command.BudgetCommandService;
import com.payv.budget.application.command.model.DeactivateBudgetCommand;
import com.payv.budget.application.port.ClassificationQueryPort;
import com.payv.budget.application.query.BudgetQueryService;
import com.payv.budget.domain.model.BudgetId;
import com.payv.budget.presentation.dto.request.BudgetListConditionRequest;
import com.payv.budget.presentation.dto.request.BudgetListNoticeRequest;
import com.payv.budget.presentation.dto.request.CreateBudgetRequest;
import com.payv.budget.presentation.dto.request.UpdateBudgetRequest;
import com.payv.common.presentation.api.AjaxResponses;
import com.payv.iam.infrastructure.security.IamUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.Map;

@Controller
@RequestMapping("/budget/budgets")
@RequiredArgsConstructor
public class BudgetViewController {

    private final BudgetCommandService commandService;
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
        BudgetQueryService.BudgetView budget = queryService.get(BudgetId.of(budgetId), ownerUserId)
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
        BudgetQueryService.BudgetView budget = queryService.get(BudgetId.of(budgetId), ownerUserId)
                .orElseThrow(() -> new IllegalStateException("budget not found"));

        model.addAttribute("budget", budget);
        model.addAttribute("categories", classificationQueryPort.getAllCategories(ownerUserId));
        model.addAttribute("error", error);
        return "budget/edit";
    }

    @PostMapping(produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> create(@AuthenticationPrincipal IamUserDetails userDetails,
                                                       @ModelAttribute CreateBudgetRequest request) {
        String ownerUserId = userDetails.getUserId();
        commandService.create(request.toCommand(), ownerUserId);
        String month = request.getMonth() == null ? YearMonth.now().toString() : request.getMonth();
        return AjaxResponses.okRedirect("/budget/budgets?created=true&month=" + month);
    }

    @PutMapping(path = "/{budgetId}", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> update(@AuthenticationPrincipal IamUserDetails userDetails,
                                                       @PathVariable String budgetId,
                                                       @RequestBody UpdateBudgetRequest request) {
        String ownerUserId = userDetails.getUserId();
        commandService.update(request.toCommand(budgetId), ownerUserId);
        String month = request.getMonth() == null ? YearMonth.now().toString() : request.getMonth();
        return AjaxResponses.okRedirect("/budget/budgets?updated=true&month=" + month);
    }

    @DeleteMapping(path = "/{budgetId}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deactivate(@AuthenticationPrincipal IamUserDetails userDetails,
                                                           @PathVariable String budgetId) {
        String ownerUserId = userDetails.getUserId();
        commandService.deactivate(new DeactivateBudgetCommand(BudgetId.of(budgetId)), ownerUserId);
        return AjaxResponses.okRedirect("/budget/budgets?deactivated=true");
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
