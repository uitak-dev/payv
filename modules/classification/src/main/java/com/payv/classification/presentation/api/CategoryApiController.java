package com.payv.classification.presentation.api;

import com.payv.classification.application.command.CategoryCommandService;
import com.payv.classification.application.command.model.DeactivateChildCategoryCommand;
import com.payv.classification.application.command.model.DeactivateRootCategoryCommand;
import com.payv.classification.domain.model.CategoryId;
import com.payv.classification.presentation.dto.request.CreateChildCategoryRequest;
import com.payv.classification.presentation.dto.request.CreateParentCategoryRequest;
import com.payv.classification.presentation.dto.request.RenameChildCategoryRequest;
import com.payv.classification.presentation.dto.request.RenameRootCategoryRequest;
import com.payv.common.presentation.api.AjaxResponses;
import com.payv.iam.infrastructure.security.IamUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/classification/categories")
@RequiredArgsConstructor
public class CategoryApiController {

    private final CategoryCommandService commandService;

    @PostMapping(path = "/roots", produces = "application/json")
    public ResponseEntity<Map<String, Object>> createRoot(@AuthenticationPrincipal IamUserDetails userDetails,
                                                           @ModelAttribute CreateParentCategoryRequest request) {
        String ownerUserId = userDetails.getUserId();
        commandService.createParent(request.toCommand(), ownerUserId);
        return AjaxResponses.okRedirect("/classification/categories?createdRoot=true");
    }

    @PostMapping(path = "/roots/{rootId}/children", produces = "application/json")
    public ResponseEntity<Map<String, Object>> createChild(@AuthenticationPrincipal IamUserDetails userDetails,
                                                            @PathVariable String rootId,
                                                            @ModelAttribute CreateChildCategoryRequest request) {
        String ownerUserId = userDetails.getUserId();
        commandService.createChild(request.toCommand(rootId), ownerUserId);
        return AjaxResponses.okRedirect("/classification/categories/roots/" + rootId + "?createdChild=true");
    }

    @PutMapping(path = "/roots/{rootId}", produces = "application/json")
    public ResponseEntity<Map<String, Object>> renameRoot(@AuthenticationPrincipal IamUserDetails userDetails,
                                                           @PathVariable String rootId,
                                                           RenameRootCategoryRequest request) {
        String ownerUserId = userDetails.getUserId();
        commandService.renameRoot(request.toCommand(rootId), ownerUserId);
        return AjaxResponses.okRedirect("/classification/categories/roots/" + rootId + "?renamed=true");
    }

    @PutMapping(path = "/roots/{rootId}/children/{childId}", produces = "application/json")
    public ResponseEntity<Map<String, Object>> renameChild(@AuthenticationPrincipal IamUserDetails userDetails,
                                                            @PathVariable String rootId,
                                                            @PathVariable String childId,
                                                            RenameChildCategoryRequest request) {
        String ownerUserId = userDetails.getUserId();
        commandService.renameChild(request.toCommand(rootId, childId), ownerUserId);
        return AjaxResponses.okRedirect("/classification/categories/roots/" + rootId + "?renamed=true");
    }

    @DeleteMapping(path = "/roots/{rootId}", produces = "application/json")
    public ResponseEntity<Map<String, Object>> deactivateRoot(@AuthenticationPrincipal IamUserDetails userDetails,
                                                               @PathVariable String rootId) {
        String ownerUserId = userDetails.getUserId();
        commandService.deactivateRoot(new DeactivateRootCategoryCommand(CategoryId.of(rootId)), ownerUserId);
        return AjaxResponses.okRedirect("/classification/categories?deactivated=true");
    }

    @DeleteMapping(path = "/roots/{rootId}/children/{childId}", produces = "application/json")
    public ResponseEntity<Map<String, Object>> deactivateChild(@AuthenticationPrincipal IamUserDetails userDetails,
                                                                @PathVariable String rootId,
                                                                @PathVariable String childId) {
        String ownerUserId = userDetails.getUserId();
        commandService.deactivateChild(
                new DeactivateChildCategoryCommand(CategoryId.of(rootId), CategoryId.of(childId)),
                ownerUserId
        );
        return AjaxResponses.okRedirect("/classification/categories/roots/" + rootId + "?deactivated=true");
    }
}
