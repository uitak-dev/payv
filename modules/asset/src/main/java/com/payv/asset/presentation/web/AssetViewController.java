package com.payv.asset.presentation.web;

import com.payv.asset.application.query.AssetQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/asset/assets")
@RequiredArgsConstructor
public class AssetViewController {

    private final AssetQueryService queryService;

    @GetMapping
    public String list(@RequestHeader("X-User-Id") String ownerUserId,
                       Model model) {

        model.addAttribute("assets", queryService.getAll(ownerUserId));
        return "asset/list";
    }
}
