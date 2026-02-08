package com.payv.classification.presentation.web;

import com.payv.classification.application.query.TagQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/classification/tags")
@RequiredArgsConstructor
public class TagViewController {

    private final TagQueryService queryService;

    @GetMapping
    public String list(Principal principal,
                       Model model) {
        String ownerUserId = principal.getName();

        model.addAttribute("tags", queryService.getAll(ownerUserId));
        return "classification/tag/list";
    }

}
