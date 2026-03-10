package com.payv.automation.presentation.api;

import com.payv.automation.application.command.FixedExpenseCommandService;
import com.payv.automation.application.command.model.DeactivateFixedExpenseCommand;
import com.payv.automation.domain.model.FixedExpenseDefinitionId;
import com.payv.automation.presentation.dto.request.CreateFixedExpenseRequest;
import com.payv.automation.presentation.dto.request.UpdateFixedExpenseRequest;
import com.payv.common.presentation.api.AjaxResponses;
import com.payv.iam.infrastructure.security.IamUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/automation/fixed-expenses")
@RequiredArgsConstructor
public class FixedExpenseApiController {

    private final FixedExpenseCommandService commandService;

    @PostMapping(produces = "application/json")
    public ResponseEntity<Map<String, Object>> create(@AuthenticationPrincipal IamUserDetails userDetails,
                                                       @ModelAttribute CreateFixedExpenseRequest request) {
        String ownerUserId = userDetails.getUserId();
        FixedExpenseDefinitionId definitionId = commandService.create(request.toCommand(), ownerUserId);
        return AjaxResponses.okRedirect("/automation/fixed-expenses/" + definitionId.getValue() + "?created=true");
    }

    @PutMapping(path = "/{definitionId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Map<String, Object>> update(@AuthenticationPrincipal IamUserDetails userDetails,
                                                       @PathVariable String definitionId,
                                                       @RequestBody UpdateFixedExpenseRequest request) {
        String ownerUserId = userDetails.getUserId();
        commandService.update(request.toCommand(definitionId), ownerUserId);
        return AjaxResponses.okRedirect("/automation/fixed-expenses/" + definitionId + "?updated=true");
    }

    @DeleteMapping(path = "/{definitionId}", produces = "application/json")
    public ResponseEntity<Map<String, Object>> deactivate(@AuthenticationPrincipal IamUserDetails userDetails,
                                                           @PathVariable String definitionId) {
        String ownerUserId = userDetails.getUserId();
        commandService.deactivate(new DeactivateFixedExpenseCommand(FixedExpenseDefinitionId.of(definitionId)), ownerUserId);
        return AjaxResponses.okRedirect("/automation/fixed-expenses?deactivated=true");
    }
}
