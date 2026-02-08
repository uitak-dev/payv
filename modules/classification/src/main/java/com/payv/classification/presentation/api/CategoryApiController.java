package com.payv.classification.presentation.api;

import com.payv.classification.application.command.CategoryCommandService;
import com.payv.classification.application.command.model.CreateChildCategoryCommand;
import com.payv.classification.application.command.model.CreateParentCategoryCommand;
import com.payv.classification.application.command.model.DeactivateChildCategoryCommand;
import com.payv.classification.application.command.model.DeactivateRootCategoryCommand;
import com.payv.classification.application.command.model.RenameChildCategoryCommand;
import com.payv.classification.application.command.model.RenameRootCategoryCommand;
import com.payv.classification.domain.model.CategoryId;
import com.payv.classification.presentation.dto.request.CreateChildCategoryRequest;
import com.payv.classification.presentation.dto.request.CreateParentCategoryRequest;
import com.payv.classification.presentation.dto.request.RenameChildCategoryRequest;
import com.payv.classification.presentation.dto.request.RenameRootCategoryRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/classification/categories")
@RequiredArgsConstructor
public class CategoryApiController {

    private final CategoryCommandService commandService;

    @PostMapping("/roots")
    public ResponseEntity<CreateCategoryResponse> createRoot(Principal principal,
                                                             @RequestBody CreateParentCategoryRequest req) {
        String ownerUserId = principal.getName();

        CategoryId id = commandService.createParent(
                new CreateParentCategoryCommand(req.getName()),
                ownerUserId
        );

        return ResponseEntity.status(201).body(new CreateCategoryResponse(id.getValue()));
    }

    @PostMapping("/roots/{rootId}/children")
    public ResponseEntity<CreateCategoryResponse> createChild(Principal principal,
                                                              @PathVariable String rootId,
                                                              @RequestBody CreateChildCategoryRequest req) {
        String ownerUserId = principal.getName();

        CategoryId id = commandService.createChild(
                new CreateChildCategoryCommand(CategoryId.of(rootId), req.getName()),
                ownerUserId
        );

        return ResponseEntity.status(201).body(new CreateCategoryResponse(id.getValue()));
    }

    @PutMapping("/roots/{rootId}")
    public ResponseEntity<Void> renameRoot(Principal principal,
                                           @PathVariable String rootId,
                                           @RequestBody RenameRootCategoryRequest req) {
        String ownerUserId = principal.getName();

        commandService.renameRoot(
                new RenameRootCategoryCommand(CategoryId.of(rootId), req.getNewName()),
                ownerUserId
        );

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/roots/{rootId}/children/{childId}")
    public ResponseEntity<Void> renameChild(Principal principal,
                                            @PathVariable String rootId,
                                            @PathVariable String childId,
                                            @RequestBody RenameChildCategoryRequest req) {
        String ownerUserId = principal.getName();

        commandService.renameChild(
                new RenameChildCategoryCommand(CategoryId.of(rootId), CategoryId.of(childId), req.getNewName()),
                ownerUserId
        );

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/roots/{rootId}")
    public ResponseEntity<Void> deactivateRoot(Principal principal,
                                               @PathVariable String rootId) {
        String ownerUserId = principal.getName();

        commandService.deactivateRoot(
                new DeactivateRootCategoryCommand(CategoryId.of(rootId)),
                ownerUserId
        );

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/roots/{rootId}/children/{childId}")
    public ResponseEntity<Void> deactivateChild(Principal principal,
                                                @PathVariable String rootId,
                                                @PathVariable String childId) {
        String ownerUserId = principal.getName();

        commandService.deactivateChild(
                new DeactivateChildCategoryCommand(CategoryId.of(rootId), CategoryId.of(childId)),
                ownerUserId
        );

        return ResponseEntity.noContent().build();
    }

    @Data
    @AllArgsConstructor
    public static class CreateCategoryResponse {
        private final String categoryId;
    }
}
