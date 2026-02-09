package com.payv.asset.presentation.web;

import com.payv.asset.application.command.AssetCommandService;
import com.payv.asset.application.command.model.CreateAssetCommand;
import com.payv.asset.application.command.model.DeactivateAssetCommand;
import com.payv.asset.application.query.AssetQueryService;
import com.payv.asset.domain.model.AssetId;
import com.payv.asset.domain.model.AssetType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/asset/assets")
@RequiredArgsConstructor
public class AssetViewController {

    private final AssetCommandService commandService;
    private final AssetQueryService queryService;

    @GetMapping
    public String list(Principal principal,
                       @RequestParam(required = false) String created,
                       @RequestParam(required = false) String deactivated,
                       @RequestParam(required = false) String error,
                       Model model) {
        String ownerUserId = principal.getName();

        model.addAttribute("assets", queryService.getAll(ownerUserId));
        model.addAttribute("created", created);
        model.addAttribute("deactivated", deactivated);
        model.addAttribute("error", error);
        return "asset/list";
    }

    @PostMapping(produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> create(Principal principal,
                                                      @RequestParam String name,
                                                      @RequestParam String assetType) {
        String ownerUserId = principal.getName();
        try {
            commandService.create(
                    new CreateAssetCommand(name, AssetType.valueOf(assetType)),
                    ownerUserId
            );
            return okRedirect("/asset/assets?created=true");
        } catch (RuntimeException e) {
            return badRequest(e.getMessage());
        }
    }

    @DeleteMapping(path = "/{assetId}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deactivate(Principal principal,
                                                           @PathVariable String assetId) {
        String ownerUserId = principal.getName();
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
