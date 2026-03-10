package com.payv.ledger.presentation.web;

import com.payv.iam.infrastructure.security.IamUserDetails;
import com.payv.ledger.application.exception.TransactionNotFoundException;
import com.payv.ledger.application.port.AssetQueryPort;
import com.payv.ledger.application.port.ClassificationQueryPort;
import com.payv.ledger.application.query.TransactionQueryService;
import com.payv.ledger.domain.model.TransactionId;
import com.payv.ledger.presentation.dto.request.TransactionDetailNoticeQueryRequest;
import com.payv.ledger.presentation.dto.request.TransactionListNoticeQueryRequest;
import com.payv.ledger.presentation.dto.request.TransactionListQueryRequest;
import com.payv.ledger.presentation.dto.viewmodel.TransactionDetailView;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/ledger/transactions")
@RequiredArgsConstructor
public class TransactionViewController {

    private final TransactionQueryService queryService;
    private final AssetQueryPort assetQueryPort;
    private final ClassificationQueryPort classificationQueryPort;

    @GetMapping
    public String list(@AuthenticationPrincipal IamUserDetails userDetails,
                       @ModelAttribute("condition") TransactionListQueryRequest condition,
                       @ModelAttribute("notice") TransactionListNoticeQueryRequest notice,
                       Model model) {
        String ownerUserId = userDetails.getUserId();

        model.addAttribute("result", queryService.list(
                ownerUserId,
                condition.getFrom(),
                condition.getTo(),
                condition.normalizedAssetId(),
                condition.getPage(),
                condition.getSize()
        ));
        model.addAttribute("assets", assetQueryPort.getAllActiveAssets(ownerUserId));
        model.addAttribute("from", condition.getFrom());
        model.addAttribute("to", condition.getTo());
        model.addAttribute("assetId", condition.normalizedAssetId());
        model.addAttribute("notice", notice);
        return "ledger/transaction/list";
    }

    @GetMapping("/new")
    public String createForm(@AuthenticationPrincipal IamUserDetails userDetails,
                             @RequestParam(required = false) String type,
                             @RequestParam(required = false) String error,
                             Model model) {
        String ownerUserId = userDetails.getUserId();
        populateFormOptions(model, ownerUserId);
        model.addAttribute("error", error);
        model.addAttribute("mode", "create");
        model.addAttribute("requestedType", normalizeRequestedType(type));
        model.addAttribute("action", "/api/ledger/transactions");
        model.addAttribute("submitLabel", "저장");
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("selectedTagMap", Collections.emptyMap());
        return "ledger/transaction/form";
    }

    @GetMapping("/{transactionId}")
    public String detail(@AuthenticationPrincipal IamUserDetails userDetails,
                         @PathVariable String transactionId,
                         @ModelAttribute("notice") TransactionDetailNoticeQueryRequest notice,
                         Model model) {
        String ownerUserId = userDetails.getUserId();
        try {
            model.addAttribute("tx", queryService.detail(TransactionId.of(transactionId), ownerUserId));
            model.addAttribute("notice", notice);
            return "ledger/transaction/detail";
        } catch (TransactionNotFoundException e) {
            return "redirect:/ledger/transactions?error=true";
        }
    }

    @GetMapping("/{transactionId}/edit")
    public String editForm(@AuthenticationPrincipal IamUserDetails userDetails,
                           @PathVariable String transactionId,
                           @RequestParam(required = false) String error,
                           Model model) {
        String ownerUserId = userDetails.getUserId();
        TransactionDetailView tx;
        try {
            tx = queryService.detail(TransactionId.of(transactionId), ownerUserId);
        } catch (TransactionNotFoundException e) {
            return "redirect:/ledger/transactions?error=true";
        }

        populateFormOptions(model, ownerUserId);
        model.addAttribute("error", error);
        model.addAttribute("mode", "edit");
        model.addAttribute("tx", tx);
        model.addAttribute("action", "/api/ledger/transactions/" + transactionId);
        model.addAttribute("submitLabel", "수정");
        model.addAttribute("selectedTagMap", toTagSelectionMap(tx));
        return "ledger/transaction/form";
    }

    private void populateFormOptions(Model model, String ownerUserId) {
        model.addAttribute("assets", assetQueryPort.getAllActiveAssets(ownerUserId));
        model.addAttribute("categories", classificationQueryPort.getAllCategories(ownerUserId));
        model.addAttribute("tags", classificationQueryPort.getAllTags(ownerUserId));
    }

    private Map<String, Boolean> toTagSelectionMap(TransactionDetailView tx) {
        Map<String, Boolean> ret = new LinkedHashMap<>();
        if (tx == null || tx.getTags() == null) return ret;
        for (TransactionDetailView.TagView tag : tx.getTags()) {
            ret.put(tag.getTagId(), true);
        }
        return ret;
    }

    private String normalizeRequestedType(String type) {
        if (type == null) return null;
        String normalized = type.trim().toUpperCase();
        if ("TRANSFER".equals(normalized)) return normalized;
        if ("INCOME".equals(normalized)) return normalized;
        if ("EXPENSE".equals(normalized)) return normalized;
        return null;
    }
}
