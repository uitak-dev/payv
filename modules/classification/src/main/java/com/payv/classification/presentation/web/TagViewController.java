package com.payv.classification.presentation.web;

import com.payv.classification.application.command.TagCommandService;
import com.payv.classification.application.command.model.CreateTagCommand;
import com.payv.classification.application.command.model.DeactivateTagCommand;
import com.payv.classification.application.query.TagQueryService;
import com.payv.classification.domain.model.TagId;
import com.payv.classification.presentation.dto.request.RenameTagRequest;
import com.payv.iam.infrastructure.security.IamUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/classification/tags")
@RequiredArgsConstructor
public class TagViewController {

    private final TagCommandService commandService;
    private final TagQueryService queryService;

    @GetMapping
    public String list(@AuthenticationPrincipal IamUserDetails userDetails,
                       @RequestParam(required = false) String created,
                       @RequestParam(required = false) String renamed,
                       @RequestParam(required = false) String deactivated,
                       @RequestParam(required = false) String error,
                       Model model) {
        String ownerUserId = userDetails.getUserId();

        model.addAttribute("tags", queryService.getAll(ownerUserId));
        model.addAttribute("created", created);
        model.addAttribute("renamed", renamed);
        model.addAttribute("deactivated", deactivated);
        model.addAttribute("error", error);
        return "classification/tag/list";
    }

    @GetMapping("/new")
    public String createForm(@RequestParam(required = false) String error, Model model) {
        model.addAttribute("error", error);
        return "classification/tag/create";
    }

    @GetMapping("/{tagId}/edit")
    public String editForm(@AuthenticationPrincipal IamUserDetails userDetails,
                           @PathVariable String tagId,
                           @RequestParam(required = false) String error,
                           Model model) {
        String ownerUserId = userDetails.getUserId();
        model.addAttribute("tag", queryService.get(TagId.of(tagId), ownerUserId)
                .orElseThrow(() -> new IllegalStateException("tag not found")));
        model.addAttribute("error", error);
        return "classification/tag/edit";
    }

    @PostMapping(produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> create(@AuthenticationPrincipal IamUserDetails userDetails,
                                                      @RequestParam String name) {
        String ownerUserId = userDetails.getUserId();
        try {
            commandService.create(new CreateTagCommand(name), ownerUserId);
            return okRedirect("/classification/tags?created=true");
        } catch (RuntimeException e) {
            return badRequest(e.getMessage());
        }
    }

    @PutMapping(path = "/{tagId}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> rename(@AuthenticationPrincipal IamUserDetails userDetails,
                                                       @PathVariable String tagId,
                                                       RenameTagRequest request) {
        String ownerUserId = userDetails.getUserId();
        try {
            commandService.rename(request.toCommand(tagId), ownerUserId);
            return okRedirect("/classification/tags?renamed=true");
        } catch (RuntimeException e) {
            return badRequest(e.getMessage());
        }
    }

    @DeleteMapping(path = "/{tagId}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deactivate(@AuthenticationPrincipal IamUserDetails userDetails,
                                                          @PathVariable String tagId) {
        String ownerUserId = userDetails.getUserId();
        try {
            commandService.deactivate(new DeactivateTagCommand(TagId.of(tagId)), ownerUserId);
            return okRedirect("/classification/tags?deactivated=true");
        } catch (RuntimeException e) {
            return badRequest(e.getMessage());
        }
    }

    private ResponseEntity<Map<String, Object>> okRedirect(String redirectPath) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("redirectUrl", redirectPath);
        return ResponseEntity.ok(body);
    }

    private ResponseEntity<Map<String, Object>> badRequest(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("message", message == null ? "request failed" : message);
        return ResponseEntity.badRequest().body(body);
    }
}
