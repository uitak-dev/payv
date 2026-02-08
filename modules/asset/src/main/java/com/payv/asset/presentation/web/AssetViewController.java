package com.payv.asset.presentation.web;

import com.payv.asset.application.query.AssetQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/asset/assets")
@RequiredArgsConstructor
public class AssetViewController {

    private final AssetQueryService queryService;

    @GetMapping
    public String list(Principal principal,
                       Model model) {
        String ownerUserId = principal.getName();

        model.addAttribute("assets", queryService.getAll(ownerUserId));
        return "asset/list";
    }
}
