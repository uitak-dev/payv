package com.payv.ledger.presentation.web;

import com.payv.iam.infrastructure.security.IamUserDetails;
import com.payv.ledger.application.exception.TransferNotFoundException;
import com.payv.ledger.application.port.AssetQueryPort;
import com.payv.ledger.application.query.TransferQueryService;
import com.payv.ledger.presentation.dto.request.TransferDetailNoticeQueryRequest;
import com.payv.ledger.presentation.dto.request.TransferListNoticeQueryRequest;
import com.payv.ledger.presentation.dto.request.TransferListQueryRequest;
import com.payv.ledger.presentation.dto.viewmodel.TransferDetailView;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/ledger/transfers")
@RequiredArgsConstructor
public class TransferViewController {

    private final TransferQueryService queryService;
    private final AssetQueryPort assetQueryPort;

    @GetMapping
    public String list(@AuthenticationPrincipal IamUserDetails userDetails,
                       @ModelAttribute("condition") TransferListQueryRequest condition,
                       @ModelAttribute("notice") TransferListNoticeQueryRequest notice,
                       Model model) {
        String ownerUserId = userDetails.getUserId();

        model.addAttribute("result", queryService.list(
                ownerUserId,
                condition.getFrom(),
                condition.getTo(),
                condition.getPage(),
                condition.getSize()
        ));
        model.addAttribute("assets", assetQueryPort.getAllActiveAssets(ownerUserId));
        model.addAttribute("from", condition.getFrom());
        model.addAttribute("to", condition.getTo());
        model.addAttribute("notice", notice);
        return "ledger/transfer/list";
    }

    @GetMapping("/new")
    public String createForm(@AuthenticationPrincipal IamUserDetails userDetails,
                             @RequestParam(required = false) String error,
                             Model model) {
        String ownerUserId = userDetails.getUserId();
        model.addAttribute("assets", assetQueryPort.getAllActiveAssets(ownerUserId));
        model.addAttribute("error", error);
        model.addAttribute("mode", "create");
        model.addAttribute("action", "/api/ledger/transfers");
        model.addAttribute("submitLabel", "저장");
        model.addAttribute("today", LocalDate.now());
        return "ledger/transfer/form";
    }

    @GetMapping("/{transferId}")
    public String detail(@AuthenticationPrincipal IamUserDetails userDetails,
                         @PathVariable String transferId,
                         @ModelAttribute("notice") TransferDetailNoticeQueryRequest notice,
                         Model model) {
        String ownerUserId = userDetails.getUserId();
        try {
            model.addAttribute("transfer", queryService.detail(transferId, ownerUserId));
            model.addAttribute("notice", notice);
            return "ledger/transfer/detail";
        } catch (TransferNotFoundException e) {
            return "redirect:/ledger/transfers?error=true";
        }
    }

    @GetMapping("/{transferId}/edit")
    public String editForm(@AuthenticationPrincipal IamUserDetails userDetails,
                           @PathVariable String transferId,
                           @RequestParam(required = false) String error,
                           Model model) {
        String ownerUserId = userDetails.getUserId();
        try {
            TransferDetailView transfer = queryService.detail(transferId, ownerUserId);
            model.addAttribute("assets", assetQueryPort.getAllActiveAssets(ownerUserId));
            model.addAttribute("error", error);
            model.addAttribute("mode", "edit");
            model.addAttribute("transfer", transfer);
            model.addAttribute("action", "/api/ledger/transfers/" + transferId);
            model.addAttribute("submitLabel", "수정");
            return "ledger/transfer/form";
        } catch (TransferNotFoundException e) {
            return "redirect:/ledger/transfers?error=true";
        }
    }
}
