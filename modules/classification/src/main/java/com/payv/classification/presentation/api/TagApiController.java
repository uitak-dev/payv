package com.payv.classification.presentation.api;

import com.payv.classification.application.command.TagCommandService;
import com.payv.classification.application.command.model.DeactivateTagCommand;
import com.payv.classification.domain.model.TagId;
import com.payv.classification.presentation.dto.request.CreateTagRequest;
import com.payv.classification.presentation.dto.request.RenameTagRequest;
import com.payv.common.presentation.api.AjaxResponses;
import com.payv.iam.infrastructure.security.IamUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/classification/tags")
@RequiredArgsConstructor
public class TagApiController {

    private final TagCommandService commandService;

    @PostMapping(produces = "application/json")
    public ResponseEntity<Map<String, Object>> create(@AuthenticationPrincipal IamUserDetails userDetails,
                                                       @ModelAttribute CreateTagRequest request) {
        String ownerUserId = userDetails.getUserId();
        commandService.create(request.toCommand(), ownerUserId);
        return AjaxResponses.okRedirect("/classification/tags?created=true");
    }

    @PutMapping(path = "/{tagId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Map<String, Object>> rename(@AuthenticationPrincipal IamUserDetails userDetails,
                                                       @PathVariable String tagId,
                                                       @RequestBody RenameTagRequest request) {
        String ownerUserId = userDetails.getUserId();
        commandService.rename(request.toCommand(tagId), ownerUserId);
        return AjaxResponses.okRedirect("/classification/tags?renamed=true");
    }

    @DeleteMapping(path = "/{tagId}", produces = "application/json")
    public ResponseEntity<Map<String, Object>> deactivate(@AuthenticationPrincipal IamUserDetails userDetails,
                                                           @PathVariable String tagId) {
        String ownerUserId = userDetails.getUserId();
        commandService.deactivate(new DeactivateTagCommand(TagId.of(tagId)), ownerUserId);
        return AjaxResponses.okRedirect("/classification/tags?deactivated=true");
    }
}
