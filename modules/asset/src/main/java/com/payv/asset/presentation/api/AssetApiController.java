package com.payv.asset.presentation.api;

import com.payv.asset.application.command.AssetCommandService;
import com.payv.asset.application.command.model.DeactivateAssetCommand;
import com.payv.asset.domain.model.AssetId;
import com.payv.asset.presentation.dto.request.CreateAssetRequest;
import com.payv.asset.presentation.dto.request.UpdateAssetRequest;
import com.payv.common.presentation.api.AjaxResponses;
import com.payv.iam.infrastructure.security.IamUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/asset/assets")
@RequiredArgsConstructor
public class AssetApiController {

    private final AssetCommandService commandService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@AuthenticationPrincipal IamUserDetails userDetails,
                                                      @ModelAttribute CreateAssetRequest request) {
        String ownerUserId = userDetails.getUserId();
        commandService.create(request.toCommand(), ownerUserId);
        return AjaxResponses.okRedirect("/asset/assets?created=true");
    }

    @PutMapping("/{assetId}")
    public ResponseEntity<Map<String, Object>> update(@AuthenticationPrincipal IamUserDetails userDetails,
                                                      @PathVariable String assetId,
                                                      @RequestBody UpdateAssetRequest request) {
        String ownerUserId = userDetails.getUserId();
        commandService.update(request.toCommand(assetId), ownerUserId);
        return AjaxResponses.okRedirect("/asset/assets?updated=true");
    }

    @DeleteMapping("/{assetId}")
    public ResponseEntity<Map<String, Object>> deactivate(@AuthenticationPrincipal IamUserDetails userDetails,
                                                          @PathVariable String assetId) {
        String ownerUserId = userDetails.getUserId();
        commandService.deactivate(new DeactivateAssetCommand(AssetId.of(assetId)), ownerUserId);
        return AjaxResponses.okRedirect("/asset/assets?deactivated=true");
    }
}
