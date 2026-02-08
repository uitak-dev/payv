package com.payv.asset.presentation.api;

import com.payv.asset.application.command.AssetCommandService;
import com.payv.asset.application.command.model.CreateAssetCommand;
import com.payv.asset.application.command.model.DeactivateAssetCommand;
import com.payv.asset.application.command.model.UpdateAssetCommand;
import com.payv.asset.domain.model.AssetId;
import com.payv.asset.domain.model.AssetType;
import com.payv.asset.presentation.dto.request.CreateAssetRequest;
import com.payv.asset.presentation.dto.request.UpdateAssetRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/asset/assets")
@RequiredArgsConstructor
public class AssetApiController {

    private final AssetCommandService commandService;

    @PostMapping
    public ResponseEntity<CreateAssetResponse> create(Principal principal,
                                                      @RequestBody CreateAssetRequest req) {
        String ownerUserId = principal.getName();

        AssetId id = commandService.create(
                new CreateAssetCommand(req.getName(), AssetType.valueOf(req.getAssetType())),
                ownerUserId
        );

        return ResponseEntity.status(201).body(new CreateAssetResponse(id.getValue()));
    }

    @PutMapping("/{assetId}")
    public ResponseEntity<Void> update(Principal principal,
                                       @PathVariable String assetId,
                                       @RequestBody UpdateAssetRequest req) {
        String ownerUserId = principal.getName();

        commandService.update(
                new UpdateAssetCommand(AssetId.of(assetId), req.getNewName(), AssetType.valueOf(req.getAssetType())),
                ownerUserId
        );

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{assetId}")
    public ResponseEntity<Void> deactivate(Principal principal,
                                           @PathVariable String assetId) {
        String ownerUserId = principal.getName();

        commandService.deactivate(new DeactivateAssetCommand(AssetId.of(assetId)), ownerUserId);
        return ResponseEntity.noContent().build();
    }

    @Data
    @AllArgsConstructor
    public static class CreateAssetResponse {
        private final String assetId;
    }
}
