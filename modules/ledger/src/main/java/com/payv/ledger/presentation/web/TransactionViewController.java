package com.payv.ledger.presentation.web;

import com.payv.ledger.application.command.AttachmentCommandService;
import com.payv.ledger.application.command.TransactionCommandService;
import com.payv.ledger.application.port.AssetQueryPort;
import com.payv.ledger.application.port.ClassificationQueryPort;
import com.payv.ledger.application.query.TransactionQueryService;
import com.payv.ledger.domain.model.AttachmentId;
import com.payv.ledger.domain.model.TransactionId;
import com.payv.ledger.presentation.dto.request.CreateTransactionRequest;
import com.payv.ledger.presentation.dto.request.TransactionDetailNoticeQueryRequest;
import com.payv.ledger.presentation.dto.request.TransactionListQueryRequest;
import com.payv.ledger.presentation.dto.request.TransactionListNoticeQueryRequest;
import com.payv.ledger.presentation.dto.request.UpdateTransactionRequest;
import com.payv.ledger.presentation.dto.viewmodel.TransactionDetailView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/ledger/transactions")
@RequiredArgsConstructor
public class TransactionViewController {

    private final TransactionQueryService queryService;
    private final TransactionCommandService commandService;
    private final AttachmentCommandService attachmentCommandService;

    private final AssetQueryPort assetQueryPort;
    private final ClassificationQueryPort classificationQueryPort;

    @GetMapping
    public String list(Principal principal,
                       @ModelAttribute("condition") TransactionListQueryRequest condition,
                       @ModelAttribute("notice") TransactionListNoticeQueryRequest notice,
                       Model model) {
        String ownerUserId = principal.getName();

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
    public String createForm(Principal principal,
                             @RequestParam(required = false) String error,
                             Model model) {
        String ownerUserId = principal.getName();
        populateFormOptions(model, ownerUserId);
        model.addAttribute("error", error);
        model.addAttribute("mode", "create");
        model.addAttribute("action", "/ledger/transactions");
        model.addAttribute("submitLabel", "저장");
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("selectedTagMap", Collections.emptyMap());
        return "ledger/transaction/form";
    }

    @PostMapping(produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> create(Principal principal,
                                                       @ModelAttribute CreateTransactionRequest request) {
        String ownerUserId = principal.getName();
        try {
            TransactionId id = commandService.createManual(request.toCommand(), ownerUserId);
            return okRedirect("/ledger/transactions/" + id.getValue() + "?created=true");
        } catch (RuntimeException e) {
            return badRequest(e.getMessage());
        }
    }

    @GetMapping("/{transactionId}")
    public String detail(Principal principal,
                         @PathVariable String transactionId,
                         @ModelAttribute("notice") TransactionDetailNoticeQueryRequest notice,
                         Model model) {
        String ownerUserId = principal.getName();

        model.addAttribute("tx", queryService.detail(TransactionId.of(transactionId), ownerUserId));
        model.addAttribute("notice", notice);
        return "ledger/transaction/detail";
    }

    @GetMapping("/{transactionId}/edit")
    public String editForm(Principal principal,
                           @PathVariable String transactionId,
                           @RequestParam(required = false) String error,
                           Model model) {
        String ownerUserId = principal.getName();
        TransactionDetailView tx = queryService.detail(TransactionId.of(transactionId), ownerUserId);

        populateFormOptions(model, ownerUserId);
        model.addAttribute("error", error);
        model.addAttribute("mode", "edit");
        model.addAttribute("tx", tx);
        model.addAttribute("action", "/ledger/transactions/" + transactionId);
        model.addAttribute("submitLabel", "수정");
        model.addAttribute("selectedTagMap", toTagSelectionMap(tx));
        return "ledger/transaction/form";
    }

    @PutMapping(path = "/{transactionId}", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> update(Principal principal,
                                                       @PathVariable String transactionId,
                                                       @RequestBody UpdateTransactionRequest request) {
        String ownerUserId = principal.getName();
        try {
            commandService.updateTransaction(TransactionId.of(transactionId), request.toCommand(), ownerUserId);
            return okRedirect("/ledger/transactions/" + transactionId + "?updated=true");
        } catch (RuntimeException e) {
            return badRequest(e.getMessage());
        }
    }

    @DeleteMapping(path = "/{transactionId}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> delete(Principal principal,
                                                       @PathVariable String transactionId) {
        String ownerUserId = principal.getName();
        try {
            commandService.deleteTransaction(TransactionId.of(transactionId), ownerUserId);
            return okRedirect("/ledger/transactions?deleted=true");
        } catch (RuntimeException e) {
            return badRequest(e.getMessage());
        }
    }

    @PostMapping(path = "/{transactionId}/attachments", consumes = "multipart/form-data", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadAttachment(Principal principal,
                                                                @PathVariable String transactionId,
                                                                @RequestParam("file") MultipartFile file) {
        String ownerUserId = principal.getName();
        try {
            attachmentCommandService.upload(TransactionId.of(transactionId), ownerUserId, file);
            return okRedirect("/ledger/transactions/" + transactionId + "?attachmentUploaded=true");
        } catch (RuntimeException e) {
            return badRequest(e.getMessage());
        }
    }

    @DeleteMapping(path = "/{transactionId}/attachments/{attachmentId}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteAttachment(Principal principal,
                                                                @PathVariable String transactionId,
                                                                @PathVariable String attachmentId) {
        String ownerUserId = principal.getName();
        try {
            attachmentCommandService.delete(AttachmentId.of(attachmentId), ownerUserId);
            return okRedirect("/ledger/transactions/" + transactionId + "?attachmentDeleted=true");
        } catch (RuntimeException e) {
            return badRequest(e.getMessage());
        }
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
