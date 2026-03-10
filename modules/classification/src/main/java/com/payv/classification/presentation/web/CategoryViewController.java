package com.payv.classification.presentation.web;

import com.payv.classification.application.query.CategoryQueryService;
import com.payv.classification.application.query.model.CategoryChildView;
import com.payv.classification.application.query.model.CategoryTreeView;
import com.payv.classification.domain.model.CategoryId;
import com.payv.iam.infrastructure.security.IamUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/classification/categories")
@RequiredArgsConstructor
public class CategoryViewController {

    private final CategoryQueryService queryService;

    @GetMapping
    public String list(@AuthenticationPrincipal IamUserDetails userDetails,
                       @RequestParam(required = false) String createdRoot,
                       @RequestParam(required = false) String createdChild,
                       @RequestParam(required = false) String renamed,
                       @RequestParam(required = false) String deactivated,
                       @RequestParam(required = false) String error,
                       Model model) {
        String ownerUserId = userDetails.getUserId();

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
    public String detail(@AuthenticationPrincipal IamUserDetails userDetails,
                         @PathVariable String rootId,
                         @RequestParam(required = false) String createdChild,
                         @RequestParam(required = false) String renamed,
                         @RequestParam(required = false) String deactivated,
                         @RequestParam(required = false) String error,
                         Model model) {
        String ownerUserId = userDetails.getUserId();
        CategoryTreeView root = getRoot(rootId, ownerUserId);

        model.addAttribute("root", root);
        model.addAttribute("createdChild", createdChild);
        model.addAttribute("renamed", renamed);
        model.addAttribute("deactivated", deactivated);
        model.addAttribute("error", error);
        return "classification/category/detail";
    }

    @GetMapping("/roots/{rootId}/edit")
    public String editRootForm(@AuthenticationPrincipal IamUserDetails userDetails,
                               @PathVariable String rootId,
                               @RequestParam(required = false) String error,
                               Model model) {
        String ownerUserId = userDetails.getUserId();
        model.addAttribute("root", getRoot(rootId, ownerUserId));
        model.addAttribute("error", error);
        return "classification/category/edit-root";
    }

    @GetMapping("/roots/{rootId}/children/new")
    public String createChildForm(@AuthenticationPrincipal IamUserDetails userDetails,
                                  @PathVariable String rootId,
                                  @RequestParam(required = false) String error,
                                  Model model) {
        String ownerUserId = userDetails.getUserId();
        model.addAttribute("root", getRoot(rootId, ownerUserId));
        model.addAttribute("error", error);
        return "classification/category/create-child";
    }

    @GetMapping("/roots/{rootId}/children/{childId}/edit")
    public String editChildForm(@AuthenticationPrincipal IamUserDetails userDetails,
                                @PathVariable String rootId,
                                @PathVariable String childId,
                                @RequestParam(required = false) String error,
                                Model model) {
        String ownerUserId = userDetails.getUserId();
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

    private CategoryTreeView getRoot(String rootId, String ownerUserId) {
        return queryService.getRoot(CategoryId.of(rootId), ownerUserId)
                .orElseThrow(() -> new IllegalStateException("root category not found"));
    }
}
