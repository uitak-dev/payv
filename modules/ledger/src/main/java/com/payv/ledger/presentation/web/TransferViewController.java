package com.payv.ledger.presentation.web;

import com.payv.iam.infrastructure.security.IamUserDetails;
import com.payv.ledger.application.command.TransferCommandService;
import com.payv.ledger.application.port.AssetQueryPort;
import com.payv.ledger.application.query.TransferQueryService;
import com.payv.ledger.domain.model.TransferId;
import com.payv.ledger.presentation.dto.request.CreateTransferRequest;
import com.payv.ledger.presentation.dto.request.TransferDetailNoticeQueryRequest;
import com.payv.ledger.presentation.dto.request.TransferListNoticeQueryRequest;
import com.payv.ledger.presentation.dto.request.TransferListQueryRequest;
import com.payv.ledger.presentation.dto.request.UpdateTransferRequest;
import com.payv.ledger.presentation.dto.viewmodel.TransferDetailView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Controller
@RequestMapping("/ledger/transfers")
@RequiredArgsConstructor
public class TransferViewController {

    private final TransferQueryService queryService;
    private final TransferCommandService commandService;
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
        model.addAttribute("action", "/ledger/transfers");
        model.addAttribute("submitLabel", "저장");
        model.addAttribute("today", LocalDate.now());
        return "ledger/transfer/form";
    }

    @PostMapping(produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> create(@AuthenticationPrincipal IamUserDetails userDetails,
                                                       @ModelAttribute CreateTransferRequest request) {
        String ownerUserId = userDetails.getUserId();
        try {
            TransferId id = commandService.create(request.toCommand(), ownerUserId);
            return okRedirect("/ledger/transfers/" + id.getValue() + "?created=true");
        } catch (RuntimeException e) {
            return badRequest(e.getMessage());
        }
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
        } catch (NoSuchElementException e) {
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
            model.addAttribute("action", "/ledger/transfers/" + transferId);
            model.addAttribute("submitLabel", "수정");
            return "ledger/transfer/form";
        } catch (NoSuchElementException e) {
            return "redirect:/ledger/transfers?error=true";
        }
    }

    @PutMapping(path = "/{transferId}", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> update(@AuthenticationPrincipal IamUserDetails userDetails,
                                                       @PathVariable String transferId,
                                                       @RequestBody UpdateTransferRequest request) {
        String ownerUserId = userDetails.getUserId();
        try {
            commandService.update(TransferId.of(transferId), request.toCommand(), ownerUserId);
            return okRedirect("/ledger/transfers/" + transferId + "?updated=true");
        } catch (RuntimeException e) {
            return badRequest(e.getMessage());
        }
    }

    @DeleteMapping(path = "/{transferId}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> delete(@AuthenticationPrincipal IamUserDetails userDetails,
                                                       @PathVariable String transferId) {
        String ownerUserId = userDetails.getUserId();
        try {
            commandService.delete(TransferId.of(transferId), ownerUserId);
            return okRedirect("/ledger/transfers?deleted=true");
        } catch (RuntimeException e) {
            return badRequest(e.getMessage());
        }
    }

    private ResponseEntity<Map<String, Object>> okRedirect(String redirectPath) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("redirectUrl", redirectPath);
        return ResponseEntity.ok(body);
    }

    private ResponseEntity<Map<String, Object>> badRequest(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("message", message == null ? "request failed" : message);
        return ResponseEntity.badRequest().body(body);
    }
}
