package com.payv.classification.presentation.web;

import com.payv.classification.application.query.CategoryQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/classification/categories")
@RequiredArgsConstructor
public class CategoryViewController {

    private final CategoryQueryService queryService;

    @GetMapping
    public String list(Principal principal,
                       Model model) {
        String ownerUserId = principal.getName();

        model.addAttribute("categories", queryService.getAll(ownerUserId));
        return "classification/category/list";
    }

}
