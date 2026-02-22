package com.payv.ledger.presentation.web;

import com.payv.ledger.application.command.AttachmentCommandService;
import com.payv.ledger.application.command.TransferCommandService;
import com.payv.ledger.application.command.TransactionCommandService;
import com.payv.ledger.application.port.AssetQueryPort;
import com.payv.ledger.application.port.AttachmentStoragePort;
import com.payv.ledger.application.port.ClassificationQueryPort;
import com.payv.ledger.application.query.TransactionQueryService;
import com.payv.ledger.domain.model.Attachment;
import com.payv.ledger.domain.model.AttachmentId;
import com.payv.ledger.domain.model.TransferId;
import com.payv.ledger.domain.model.TransactionId;
import com.payv.ledger.presentation.dto.request.CreateTransactionRequest;
import com.payv.ledger.presentation.dto.request.TransactionDetailNoticeQueryRequest;
import com.payv.ledger.presentation.dto.request.TransactionListQueryRequest;
import com.payv.ledger.presentation.dto.request.TransactionListNoticeQueryRequest;
import com.payv.ledger.presentation.dto.request.UpdateTransactionRequest;
import com.payv.ledger.presentation.dto.viewmodel.TransactionDetailView;
import com.payv.iam.infrastructure.security.IamUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    private final TransferCommandService transferCommandService;
    private final AttachmentCommandService attachmentCommandService;
    private final AttachmentStoragePort attachmentStoragePort;

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
        model.addAttribute("action", "/ledger/transactions");
        model.addAttribute("submitLabel", "저장");
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("selectedTagMap", Collections.emptyMap());
        return "ledger/transaction/form";
    }

    @PostMapping(produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> create(@AuthenticationPrincipal IamUserDetails userDetails,
                                                       @ModelAttribute CreateTransactionRequest request) {
        String ownerUserId = userDetails.getUserId();
        try {
            if (request.isTransferType()) {
                TransferId id = transferCommandService.create(request.toTransferCommand(), ownerUserId);
                return okRedirect("/ledger/transfers/" + id.getValue() + "?created=true");
            }
            TransactionId id = commandService.createManual(request.toCommand(), ownerUserId);
            return okRedirect("/ledger/transactions/" + id.getValue() + "?created=true");
        } catch (RuntimeException e) {
            return badRequest(e.getMessage());
        }
    }

    @GetMapping("/{transactionId}")
    public String detail(@AuthenticationPrincipal IamUserDetails userDetails,
                         @PathVariable String transactionId,
                         @ModelAttribute("notice") TransactionDetailNoticeQueryRequest notice,
                         Model model) {
        String ownerUserId = userDetails.getUserId();

        model.addAttribute("tx", queryService.detail(TransactionId.of(transactionId), ownerUserId));
        model.addAttribute("notice", notice);
        return "ledger/transaction/detail";
    }

    @GetMapping("/{transactionId}/edit")
    public String editForm(@AuthenticationPrincipal IamUserDetails userDetails,
                           @PathVariable String transactionId,
                           @RequestParam(required = false) String error,
                           Model model) {
        String ownerUserId = userDetails.getUserId();
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
    public ResponseEntity<Map<String, Object>> update(@AuthenticationPrincipal IamUserDetails userDetails,
                                                       @PathVariable String transactionId,
                                                       @RequestBody UpdateTransactionRequest request) {
        String ownerUserId = userDetails.getUserId();
        try {
            if (request.isTransferType()) {
                transferCommandService.update(TransferId.of(transactionId), request.toTransferCommand(), ownerUserId);
                return okRedirect("/ledger/transfers/" + transactionId + "?updated=true");
            }
            commandService.updateTransaction(TransactionId.of(transactionId), request.toCommand(), ownerUserId);
            return okRedirect("/ledger/transactions/" + transactionId + "?updated=true");
        } catch (RuntimeException e) {
            return badRequest(e.getMessage());
        }
    }

    @DeleteMapping(path = "/{transactionId}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> delete(@AuthenticationPrincipal IamUserDetails userDetails,
                                                       @PathVariable String transactionId) {
        String ownerUserId = userDetails.getUserId();
        try {
            commandService.deleteTransaction(TransactionId.of(transactionId), ownerUserId);
            return okRedirect("/ledger/transactions?deleted=true");
        } catch (RuntimeException e) {
            return badRequest(e.getMessage());
        }
    }

    @PostMapping(path = "/{transactionId}/attachments", consumes = "multipart/form-data", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadAttachment(@AuthenticationPrincipal IamUserDetails userDetails,
                                                                @PathVariable String transactionId,
                                                                @RequestParam("file") MultipartFile file) {
        String ownerUserId = userDetails.getUserId();
        try {
            attachmentCommandService.upload(TransactionId.of(transactionId), ownerUserId, file);
            return okRedirect("/ledger/transactions/" + transactionId + "?attachmentUploaded=true");
        } catch (RuntimeException e) {
            return badRequest(e.getMessage());
        }
    }

    @DeleteMapping(path = "/{transactionId}/attachments/{attachmentId}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteAttachment(@AuthenticationPrincipal IamUserDetails userDetails,
                                                                @PathVariable String transactionId,
                                                                @PathVariable String attachmentId) {
        String ownerUserId = userDetails.getUserId();
        try {
            attachmentCommandService.delete(AttachmentId.of(attachmentId), ownerUserId);
            return okRedirect("/ledger/transactions/" + transactionId + "?attachmentDeleted=true");
        } catch (RuntimeException e) {
            return badRequest(e.getMessage());
        }
    }

    @GetMapping("/{transactionId}/attachments/{attachmentId}/image")
    @ResponseBody
    public ResponseEntity<byte[]> attachmentImage(@AuthenticationPrincipal IamUserDetails userDetails,
                                                  @PathVariable String transactionId,
                                                  @PathVariable String attachmentId) {
        String ownerUserId = userDetails.getUserId();
        try {
            Attachment attachment = queryService.findStoredAttachment(
                            TransactionId.of(transactionId), AttachmentId.of(attachmentId), ownerUserId)
                    .orElseThrow(() -> new IllegalStateException("attachment not found"));

            byte[] body = attachmentStoragePort.readFinal(
                    attachment.getStoragePath(),
                    attachment.getStoredFileName()
            );
            MediaType mediaType = parseMediaType(attachment.getContentType());
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(body);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
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

    private MediaType parseMediaType(String contentType) {
        if (contentType == null || contentType.trim().isEmpty()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        try {
            return MediaType.parseMediaType(contentType);
        } catch (IllegalArgumentException e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
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
