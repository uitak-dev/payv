package com.payv.classification.presentation.web;

import com.payv.classification.application.command.CategoryCommandService;
import com.payv.classification.application.command.model.CreateChildCategoryCommand;
import com.payv.classification.application.command.model.CreateParentCategoryCommand;
import com.payv.classification.application.command.model.DeactivateChildCategoryCommand;
import com.payv.classification.application.command.model.DeactivateRootCategoryCommand;
import com.payv.classification.application.query.model.CategoryChildView;
import com.payv.classification.application.query.CategoryQueryService;
import com.payv.classification.application.query.model.CategoryTreeView;
import com.payv.classification.domain.model.CategoryId;
import com.payv.classification.presentation.dto.request.RenameChildCategoryRequest;
import com.payv.classification.presentation.dto.request.RenameRootCategoryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/classification/categories")
@RequiredArgsConstructor
public class CategoryViewController {

    private final CategoryCommandService commandService;
    private final CategoryQueryService queryService;

    @GetMapping
    public String list(Principal principal,
                       @RequestParam(required = false) String createdRoot,
                       @RequestParam(required = false) String createdChild,
                       @RequestParam(required = false) String renamed,
                       @RequestParam(required = false) String deactivated,
                       @RequestParam(required = false) String error,
                       Model model) {
        String ownerUserId = principal.getName();

        model.addAttribute("categories", queryService.getAll(ownerUserId));
        model.addAttribute("createdRoot", createdRoot);
        model.addAttribute("createdChild", createdChild);
        model.addAttribute("renamed", renamed);
        model.addAttribute("deactivated", deactivated);
        model.addAttribute("error", error);
        return "classification/category/list";
    }

    @GetMapping("/new")
    public String createRootForm(@RequestParam(required = false) String error, Model model) {
        model.addAttribute("error", error);
        return "classification/category/create";
    }

    @GetMapping("/roots/{rootId}")
    public String detail(Principal principal,
                         @PathVariable String rootId,
                         @RequestParam(required = false) String createdChild,
                         @RequestParam(required = false) String renamed,
                         @RequestParam(required = false) String deactivated,
                         @RequestParam(required = false) String error,
                         Model model) {
        String ownerUserId = principal.getName();
        CategoryTreeView root = getRoot(rootId, ownerUserId);

        model.addAttribute("root", root);
        model.addAttribute("createdChild", createdChild);
        model.addAttribute("renamed", renamed);
        model.addAttribute("deactivated", deactivated);
        model.addAttribute("error", error);
        return "classification/category/detail";
    }

    @GetMapping("/roots/{rootId}/edit")
    public String editRootForm(Principal principal,
                               @PathVariable String rootId,
                               @RequestParam(required = false) String error,
                               Model model) {
        String ownerUserId = principal.getName();
        model.addAttribute("root", getRoot(rootId, ownerUserId));
        model.addAttribute("error", error);
        return "classification/category/edit-root";
    }

    @GetMapping("/roots/{rootId}/children/new")
    public String createChildForm(Principal principal,
                                  @PathVariable String rootId,
                                  @RequestParam(required = false) String error,
                                  Model model) {
        String ownerUserId = principal.getName();
        model.addAttribute("root", getRoot(rootId, ownerUserId));
        model.addAttribute("error", error);
        return "classification/category/create-child";
    }

    @GetMapping("/roots/{rootId}/children/{childId}/edit")
    public String editChildForm(Principal principal,
                                @PathVariable String rootId,
                                @PathVariable String childId,
                                @RequestParam(required = false) String error,
                                Model model) {
        String ownerUserId = principal.getName();
        CategoryTreeView root = getRoot(rootId, ownerUserId);
        CategoryChildView child = root.getChildren().stream()
                .filter(c -> c.getCategoryId().equals(childId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("child category not found"));

        model.addAttribute("root", root);
        model.addAttribute("child", child);
        model.addAttribute("error", error);
        return "classification/category/edit-child";
    }

    @PostMapping(path = "/roots", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createRoot(Principal principal,
                                                           @RequestParam String name) {
        String ownerUserId = principal.getName();
        try {
            commandService.createParent(new CreateParentCategoryCommand(name), ownerUserId);
            return okRedirect("/classification/categories?createdRoot=true");
        } catch (RuntimeException e) {
            return badRequest(e.getMessage());
        }
    }

    @PostMapping(path = "/roots/{rootId}/children", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createChild(Principal principal,
                                                            @PathVariable String rootId,
                                                            @RequestParam String name) {
        String ownerUserId = principal.getName();
        try {
            commandService.createChild(
                    new CreateChildCategoryCommand(CategoryId.of(rootId), name),
                    ownerUserId
            );
            return okRedirect("/classification/categories/roots/" + rootId + "?createdChild=true");
        } catch (RuntimeException e) {
            return badRequest(e.getMessage());
        }
    }

    @PutMapping(path = "/roots/{rootId}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> renameRoot(Principal principal,
                                                           @PathVariable String rootId,
                                                           RenameRootCategoryRequest request) {
        String ownerUserId = principal.getName();
        try {
            commandService.renameRoot(request.toCommand(rootId), ownerUserId);
            return okRedirect("/classification/categories/roots/" + rootId + "?renamed=true");
        } catch (RuntimeException e) {
            return badRequest(e.getMessage());
        }
    }

    @PutMapping(path = "/roots/{rootId}/children/{childId}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> renameChild(Principal principal,
                                                            @PathVariable String rootId,
                                                            @PathVariable String childId,
                                                            RenameChildCategoryRequest request) {
        String ownerUserId = principal.getName();
        try {
            commandService.renameChild(request.toCommand(rootId, childId), ownerUserId);
            return okRedirect("/classification/categories/roots/" + rootId + "?renamed=true");
        } catch (RuntimeException e) {
            return badRequest(e.getMessage());
        }
    }

    @DeleteMapping(path = "/roots/{rootId}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deactivateRoot(Principal principal,
                                                               @PathVariable String rootId) {
        String ownerUserId = principal.getName();
        try {
            commandService.deactivateRoot(
                    new DeactivateRootCategoryCommand(CategoryId.of(rootId)),
                    ownerUserId
            );
            return okRedirect("/classification/categories?deactivated=true");
        } catch (RuntimeException e) {
            return badRequest(e.getMessage());
        }
    }

    @DeleteMapping(path = "/roots/{rootId}/children/{childId}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deactivateChild(Principal principal,
                                                                @PathVariable String rootId,
                                                                @PathVariable String childId) {
        String ownerUserId = principal.getName();
        try {
            commandService.deactivateChild(
                    new DeactivateChildCategoryCommand(CategoryId.of(rootId), CategoryId.of(childId)),
                    ownerUserId
            );
            return okRedirect("/classification/categories/roots/" + rootId + "?deactivated=true");
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

    private CategoryTreeView getRoot(String rootId, String ownerUserId) {
        return queryService.getRoot(CategoryId.of(rootId), ownerUserId)
                .orElseThrow(() -> new IllegalStateException("root category not found"));
    }
}
