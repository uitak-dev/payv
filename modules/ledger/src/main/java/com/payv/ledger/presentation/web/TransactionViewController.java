package com.payv.ledger.presentation.web;

import com.payv.asset.application.query.AssetQueryService;
import com.payv.classification.application.query.CategoryQueryService;
import com.payv.classification.application.query.TagQueryService;
import com.payv.ledger.application.command.AttachmentCommandService;
import com.payv.ledger.application.command.TransactionCommandService;
import com.payv.ledger.application.command.model.CreateTransactionCommand;
import com.payv.ledger.application.command.model.UpdateTransactionCommand;
import com.payv.ledger.application.query.TransactionQueryService;
import com.payv.ledger.domain.model.AttachmentId;
import com.payv.ledger.domain.model.Money;
import com.payv.ledger.domain.model.TransactionId;
import com.payv.ledger.domain.model.TransactionType;
import com.payv.ledger.presentation.dto.viewmodel.TransactionDetailView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/ledger/transactions")
@RequiredArgsConstructor
public class TransactionViewController {

    private final TransactionQueryService queryService;
    private final TransactionCommandService commandService;
    private final AttachmentCommandService attachmentCommandService;

    private final AssetQueryService assetQueryService;
    private final CategoryQueryService categoryQueryService;
    private final TagQueryService tagQueryService;

    @GetMapping
    public String list(Principal principal,
                       @RequestParam(required = false) String from,
                       @RequestParam(required = false) String to,
                       @RequestParam(required = false) String assetId,
                       @RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "20") int size,
                       @RequestParam(required = false) String created,
                       @RequestParam(required = false) String updated,
                       @RequestParam(required = false) String deleted,
                       @RequestParam(required = false) String error,
                       Model model) {
        String ownerUserId = principal.getName();

        LocalDate fromDate = (from != null && !from.isEmpty()) ? LocalDate.parse(from) : null;
        LocalDate toDate = (to != null && !to.isEmpty()) ? LocalDate.parse(to) : null;

        model.addAttribute("result", queryService.list(ownerUserId, fromDate, toDate, assetId, page, size));
        model.addAttribute("assets", assetQueryService.getAll(ownerUserId));
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("assetId", assetId);
        model.addAttribute("created", created);
        model.addAttribute("updated", updated);
        model.addAttribute("deleted", deleted);
        model.addAttribute("error", error);
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
                                                       @RequestParam String transactionType,
                                                       @RequestParam long amount,
                                                       @RequestParam String transactionDate,
                                                       @RequestParam String assetId,
                                                       @RequestParam String categoryIdLevel1,
                                                       @RequestParam(required = false) String categoryIdLevel2,
                                                       @RequestParam(required = false) String memo,
                                                       @RequestParam(value = "tagIds", required = false) List<String> tagIds) {
        String ownerUserId = principal.getName();
        try {
            CreateTransactionCommand command = CreateTransactionCommand.builder()
                    .transactionType(TransactionType.valueOf(transactionType))
                    .amount(Money.generate(amount))
                    .transactionDate(LocalDate.parse(transactionDate))
                    .assetId(assetId)
                    .categoryIdLevel1(categoryIdLevel1)
                    .categoryIdLevel2(blankToNull(categoryIdLevel2))
                    .memo(blankToNull(memo))
                    .build();
            if (tagIds != null) {
                for (String tagId : tagIds) {
                    if (tagId != null && !tagId.trim().isEmpty()) command.addTagId(tagId);
                }
            }

            TransactionId id = commandService.createManual(command, ownerUserId);
            return okRedirect("/ledger/transactions/" + id.getValue() + "?created=true");
        } catch (RuntimeException e) {
            return badRequest(e.getMessage());
        }
    }

    @GetMapping("/{transactionId}")
    public String detail(Principal principal,
                         @PathVariable String transactionId,
                         @RequestParam(required = false) String created,
                         @RequestParam(required = false) String updated,
                         @RequestParam(required = false) String attachmentUploaded,
                         @RequestParam(required = false) String attachmentDeleted,
                         @RequestParam(required = false) String error,
                         Model model) {
        String ownerUserId = principal.getName();

        model.addAttribute("tx", queryService.detail(TransactionId.of(transactionId), ownerUserId));
        model.addAttribute("created", created);
        model.addAttribute("updated", updated);
        model.addAttribute("attachmentUploaded", attachmentUploaded);
        model.addAttribute("attachmentDeleted", attachmentDeleted);
        model.addAttribute("error", error);
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
                                                       @RequestBody Map<String, Object> req) {
        String ownerUserId = principal.getName();
        try {
            String transactionType = stringValue(req.get("transactionType"));
            long amount = Long.parseLong(stringValue(req.get("amount")));
            LocalDate transactionDate = LocalDate.parse(stringValue(req.get("transactionDate")));
            String assetId = stringValue(req.get("assetId"));
            String categoryIdLevel1 = stringValue(req.get("categoryIdLevel1"));
            String categoryIdLevel2 = blankToNull(stringValue(req.get("categoryIdLevel2")));
            String memo = blankToNull(stringValue(req.get("memo")));

            UpdateTransactionCommand command = UpdateTransactionCommand.builder()
                    .transactionType(TransactionType.valueOf(transactionType))
                    .amount(Money.generate(amount))
                    .transactionDate(transactionDate)
                    .assetId(assetId)
                    .categoryIdLevel1(categoryIdLevel1)
                    .categoryIdLevel2(categoryIdLevel2)
                    .memo(memo)
                    .build();

            for (String tagId : stringListValue(req.get("tagIds"))) {
                    if (tagId != null && !tagId.trim().isEmpty()) command.addTagId(tagId);
            }

            commandService.updateTransaction(TransactionId.of(transactionId), command, ownerUserId);
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
        model.addAttribute("assets", assetQueryService.getAll(ownerUserId));
        model.addAttribute("categories", categoryQueryService.getAll(ownerUserId));
        model.addAttribute("tags", tagQueryService.getAll(ownerUserId));
    }

    private Map<String, Boolean> toTagSelectionMap(TransactionDetailView tx) {
        Map<String, Boolean> ret = new LinkedHashMap<>();
        if (tx == null || tx.getTags() == null) return ret;
        for (TransactionDetailView.TagView tag : tx.getTags()) {
            ret.put(tag.getTagId(), true);
        }
        return ret;
    }

    private static String blankToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String stringValue(Object value) {
        if (value == null) return null;
        return String.valueOf(value);
    }

    private static List<String> stringListValue(Object value) {
        if (value == null) return Collections.emptyList();
        if (value instanceof Collection) {
            List<String> ret = new ArrayList<>();
            for (Object each : (Collection<?>) value) {
                ret.add(String.valueOf(each));
            }
            return ret;
        }
        return Collections.singletonList(String.valueOf(value));
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
