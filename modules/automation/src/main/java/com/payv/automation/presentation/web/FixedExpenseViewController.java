package com.payv.automation.presentation.web;

import com.payv.automation.application.exception.FixedExpenseNotFoundException;
import com.payv.automation.application.query.FixedExpenseQueryService;
import com.payv.automation.application.query.model.FixedExpenseView;
import com.payv.automation.domain.model.FixedExpenseDefinitionId;
import com.payv.automation.presentation.dto.request.FixedExpenseDetailNoticeRequest;
import com.payv.automation.presentation.dto.request.FixedExpenseListNoticeRequest;
import com.payv.iam.infrastructure.security.IamUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/automation/fixed-expenses")
@RequiredArgsConstructor
public class FixedExpenseViewController {

    private final FixedExpenseQueryService queryService;

    @GetMapping
    public String list(@AuthenticationPrincipal IamUserDetails userDetails,
                       @ModelAttribute("notice") FixedExpenseListNoticeRequest notice,
                       Model model) {
        String ownerUserId = userDetails.getUserId();
        model.addAttribute("fixedExpenses", queryService.getAll(ownerUserId));
        model.addAttribute("notice", notice);
        return "automation/fixed-expense/list";
    }

    @GetMapping("/new")
    public String createForm(@AuthenticationPrincipal IamUserDetails userDetails,
                             @RequestParam(required = false) String error,
                             Model model) {
        String ownerUserId = userDetails.getUserId();
        model.addAttribute("assets", queryService.getAssetOptions(ownerUserId));
        model.addAttribute("categories", queryService.getCategoryOptions(ownerUserId));
        model.addAttribute("error", error);
        model.addAttribute("mode", "create");
        model.addAttribute("action", "/api/automation/fixed-expenses");
        model.addAttribute("submitLabel", "저장");
        return "automation/fixed-expense/form";
    }

    @GetMapping("/{definitionId}")
    public String detail(@AuthenticationPrincipal IamUserDetails userDetails,
                         @PathVariable String definitionId,
                         @ModelAttribute("notice") FixedExpenseDetailNoticeRequest notice,
                         Model model) {
        String ownerUserId = userDetails.getUserId();
        FixedExpenseView fixedExpense = queryService.get(FixedExpenseDefinitionId.of(definitionId), ownerUserId)
                .orElseThrow(FixedExpenseNotFoundException::new);
        model.addAttribute("fixedExpense", fixedExpense);
        model.addAttribute("notice", notice);
        return "automation/fixed-expense/detail";
    }

    @GetMapping("/{definitionId}/edit")
    public String editForm(@AuthenticationPrincipal IamUserDetails userDetails,
                           @PathVariable String definitionId,
                           @RequestParam(required = false) String error,
                           Model model) {
        String ownerUserId = userDetails.getUserId();

        FixedExpenseView fixedExpense = queryService.get(FixedExpenseDefinitionId.of(definitionId), ownerUserId)
                .orElseThrow(FixedExpenseNotFoundException::new);

        model.addAttribute("fixedExpense", fixedExpense);
        model.addAttribute("assets", queryService.getAssetOptions(ownerUserId));
        model.addAttribute("categories", queryService.getCategoryOptions(ownerUserId));
        model.addAttribute("error", error);
        model.addAttribute("mode", "edit");
        model.addAttribute("action", "/api/automation/fixed-expenses/" + definitionId);
        model.addAttribute("submitLabel", "수정");
        return "automation/fixed-expense/form";
    }
}
