package com.payv.classification.presentation.web;

import com.payv.classification.application.query.CategoryQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;


@RequestMapping("/classification/categories")
@RequiredArgsConstructor
public class CategoryViewController {

    private final CategoryQueryService queryService;

    @GetMapping
    public String list(@RequestHeader("X-User-Id") String ownerUserId,
                       Model model) {

        model.addAttribute("categories", queryService.getAll(ownerUserId));
        return "classification/category/list";
    }

}
