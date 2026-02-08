package com.payv.classification.presentation.api;

import com.payv.classification.application.command.TagCommandService;
import com.payv.classification.application.command.model.CreateTagCommand;
import com.payv.classification.application.command.model.DeactivateTagCommand;
import com.payv.classification.application.command.model.RenameTagCommand;
import com.payv.classification.domain.model.TagId;
import com.payv.classification.presentation.dto.request.CreateTagRequest;
import com.payv.classification.presentation.dto.request.RenameTagRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/classification/tags")
@RequiredArgsConstructor
public class TagApiController {

    private final TagCommandService commandService;

    @PostMapping
    public ResponseEntity<CreateTagResponse> create(Principal principal,
                                                    @RequestBody CreateTagRequest req) {
        String ownerUserId = principal.getName();

        TagId id = commandService.create(new CreateTagCommand(req.getName()), ownerUserId);
        return ResponseEntity.status(201).body(new CreateTagResponse(id.getValue()));
    }

    @PutMapping("/{tagId}")
    public ResponseEntity<Void> rename(Principal principal,
                                       @PathVariable String tagId,
                                       @RequestBody RenameTagRequest req) {
        String ownerUserId = principal.getName();

        commandService.rename(new RenameTagCommand(TagId.of(tagId), req.getNewName()), ownerUserId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{tagId}")
    public ResponseEntity<Void> deactivate(Principal principal,
                                           @PathVariable String tagId) {
        String ownerUserId = principal.getName();

        commandService.deactivate(new DeactivateTagCommand(TagId.of(tagId)), ownerUserId);
        return ResponseEntity.noContent().build();
    }

    @Data
    @AllArgsConstructor
    public static class CreateTagResponse {
        private final String tagId;
    }
}
