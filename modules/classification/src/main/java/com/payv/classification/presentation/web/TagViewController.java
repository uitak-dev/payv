package com.payv.classification.presentation.web;

import com.payv.classification.application.query.TagQueryService;
import com.payv.classification.application.query.model.TagView;
import com.payv.classification.domain.model.TagId;
import com.payv.iam.infrastructure.security.IamUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/classification/tags")
@RequiredArgsConstructor
public class TagViewController {

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
        TagView tag = queryService.get(TagId.of(tagId), ownerUserId).orElse(null);
        if (tag == null) {
            return "redirect:/classification/tags?error=true";
        }
        model.addAttribute("tag", tag);
        model.addAttribute("error", error);
        return "classification/tag/edit";
    }
}
