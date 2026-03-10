package com.payv.budget.presentation.api;

import com.payv.budget.application.command.BudgetCommandService;
import com.payv.budget.application.command.model.DeactivateBudgetCommand;
import com.payv.budget.domain.model.BudgetId;
import com.payv.budget.presentation.dto.request.CreateBudgetRequest;
import com.payv.budget.presentation.dto.request.UpdateBudgetRequest;
import com.payv.common.presentation.api.AjaxResponses;
import com.payv.iam.infrastructure.security.IamUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.Map;

@RestController
@RequestMapping("/api/budget/budgets")
@RequiredArgsConstructor
public class BudgetApiController {

    private final BudgetCommandService commandService;

    @PostMapping(produces = "application/json")
    public ResponseEntity<Map<String, Object>> create(@AuthenticationPrincipal IamUserDetails userDetails,
                                                       @ModelAttribute CreateBudgetRequest request) {
        String ownerUserId = userDetails.getUserId();
        commandService.create(request.toCommand(), ownerUserId);
        String month = request.getMonth() == null ? YearMonth.now().toString() : request.getMonth();
        return AjaxResponses.okRedirect("/budget/budgets?created=true&month=" + month);
    }

    @PutMapping(path = "/{budgetId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Map<String, Object>> update(@AuthenticationPrincipal IamUserDetails userDetails,
                                                       @PathVariable String budgetId,
                                                       @RequestBody UpdateBudgetRequest request) {
        String ownerUserId = userDetails.getUserId();
        commandService.update(request.toCommand(budgetId), ownerUserId);
        String month = request.getMonth() == null ? YearMonth.now().toString() : request.getMonth();
        return AjaxResponses.okRedirect("/budget/budgets?updated=true&month=" + month);
    }

    @DeleteMapping(path = "/{budgetId}", produces = "application/json")
    public ResponseEntity<Map<String, Object>> deactivate(@AuthenticationPrincipal IamUserDetails userDetails,
                                                           @PathVariable String budgetId) {
        String ownerUserId = userDetails.getUserId();
        commandService.deactivate(new DeactivateBudgetCommand(BudgetId.of(budgetId)), ownerUserId);
        return AjaxResponses.okRedirect("/budget/budgets?deactivated=true");
    }
}
