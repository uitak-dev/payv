package com.payv.asset.presentation.web;

import com.payv.asset.application.command.AssetCommandService;
import com.payv.asset.application.command.model.DeactivateAssetCommand;
import com.payv.asset.application.query.AssetQueryService;
import com.payv.asset.domain.model.AssetId;
import com.payv.asset.presentation.dto.request.CreateAssetRequest;
import com.payv.asset.presentation.dto.request.UpdateAssetRequest;
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
@RequestMapping("/asset/assets")
@RequiredArgsConstructor
public class AssetViewController {

    private final AssetCommandService commandService;
    private final AssetQueryService queryService;

    @GetMapping
    public String list(@AuthenticationPrincipal IamUserDetails userDetails,
                       @RequestParam(required = false) String created,
                       @RequestParam(required = false) String updated,
                       @RequestParam(required = false) String deactivated,
                       @RequestParam(required = false) String error,
                       Model model) {
        String ownerUserId = userDetails.getUserId();

        model.addAttribute("assets", queryService.getAll(ownerUserId));
        model.addAttribute("created", created);
        model.addAttribute("updated", updated);
        model.addAttribute("deactivated", deactivated);
        model.addAttribute("error", error);
        return "asset/list";
    }

    @GetMapping("/new")
    public String createForm(@RequestParam(required = false) String error, Model model) {
        model.addAttribute("error", error);
        return "asset/create";
    }

    @GetMapping("/{assetId}/edit")
    public String editForm(@AuthenticationPrincipal IamUserDetails userDetails,
                           @PathVariable String assetId,
                           @RequestParam(required = false) String error,
                           Model model) {
        String ownerUserId = userDetails.getUserId();
        model.addAttribute("asset", queryService.get(AssetId.of(assetId), ownerUserId)
                .orElseThrow(() -> new IllegalStateException("asset not found")));
        model.addAttribute("error", error);
        return "asset/edit";
    }

    @PostMapping(produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> create(@AuthenticationPrincipal IamUserDetails userDetails,
                                                       @ModelAttribute CreateAssetRequest request) {
        String ownerUserId = userDetails.getUserId();
        try {
            commandService.create(request.toCommand(), ownerUserId);
            return okRedirect("/asset/assets?created=true");
        } catch (RuntimeException e) {
            return badRequest(e.getMessage());
        }
    }

    @PutMapping(path = "/{assetId}", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> update(@AuthenticationPrincipal IamUserDetails userDetails,
                                                       @PathVariable String assetId,
                                                       @RequestBody UpdateAssetRequest request) {
        String ownerUserId = userDetails.getUserId();
        try {
            commandService.update(request.toCommand(assetId), ownerUserId);
            return okRedirect("/asset/assets?updated=true");
        } catch (RuntimeException e) {
            return badRequest(e.getMessage());
        }
    }

    @DeleteMapping(path = "/{assetId}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deactivate(@AuthenticationPrincipal IamUserDetails userDetails,
                                                           @PathVariable String assetId) {
        String ownerUserId = userDetails.getUserId();
        try {
            commandService.deactivate(new DeactivateAssetCommand(AssetId.of(assetId)), ownerUserId);
            return okRedirect("/asset/assets?deactivated=true");
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
