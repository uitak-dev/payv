package com.payv.automation.presentation.web;

import com.payv.automation.application.command.FixedExpenseCommandService;
import com.payv.automation.application.command.model.DeactivateFixedExpenseCommand;
import com.payv.automation.application.exception.FixedExpenseNotFoundException;
import com.payv.automation.application.query.FixedExpenseQueryService;
import com.payv.automation.application.query.model.FixedExpenseView;
import com.payv.automation.domain.model.FixedExpenseDefinitionId;
import com.payv.automation.presentation.dto.request.CreateFixedExpenseRequest;
import com.payv.automation.presentation.dto.request.FixedExpenseListNoticeRequest;
import com.payv.automation.presentation.dto.request.UpdateFixedExpenseRequest;
import com.payv.common.presentation.api.AjaxResponses;
import com.payv.iam.infrastructure.security.IamUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/automation/fixed-expenses")
@RequiredArgsConstructor
public class FixedExpenseViewController {

    private final FixedExpenseCommandService commandService;
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
        return "automation/fixed-expense/create";
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
        return "automation/fixed-expense/edit";
    }

    @PostMapping(produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> create(@AuthenticationPrincipal IamUserDetails userDetails,
                                                       @ModelAttribute CreateFixedExpenseRequest request) {
        String ownerUserId = userDetails.getUserId();
        commandService.create(request.toCommand(), ownerUserId);
        return AjaxResponses.okRedirect("/automation/fixed-expenses?created=true");
    }

    @PutMapping(path = "/{definitionId}", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> update(@AuthenticationPrincipal IamUserDetails userDetails,
                                                       @PathVariable String definitionId,
                                                       @RequestBody UpdateFixedExpenseRequest request) {
        String ownerUserId = userDetails.getUserId();
        commandService.update(request.toCommand(definitionId), ownerUserId);
        return AjaxResponses.okRedirect("/automation/fixed-expenses?updated=true");
    }

    @DeleteMapping(path = "/{definitionId}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deactivate(@AuthenticationPrincipal IamUserDetails userDetails,
                                                           @PathVariable String definitionId) {
        String ownerUserId = userDetails.getUserId();
        commandService.deactivate(new DeactivateFixedExpenseCommand(FixedExpenseDefinitionId.of(definitionId)), ownerUserId);
        return AjaxResponses.okRedirect("/automation/fixed-expenses?deactivated=true");
    }
}
