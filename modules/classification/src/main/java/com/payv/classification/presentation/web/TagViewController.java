package com.payv.classification.presentation.web;

import com.payv.classification.application.query.TagQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/classification/tags")
@RequiredArgsConstructor
public class TagViewController {

    private final TagQueryService queryService;

    @GetMapping
    public String list(@RequestHeader("X-User-Id") String ownerUserId,
                       Model model) {

        model.addAttribute("tags", queryService.getAll(ownerUserId));
        return "classification/tag/list";
    }

}
