package com.payv.ledger.presentation.api;

import com.payv.common.presentation.api.AjaxResponses;
import com.payv.iam.infrastructure.security.IamUserDetails;
import com.payv.ledger.application.command.AttachmentCommandService;
import com.payv.ledger.application.command.TransactionCommandService;
import com.payv.ledger.application.command.TransferCommandService;
import com.payv.ledger.application.exception.AttachmentBinaryNotFoundException;
import com.payv.ledger.application.exception.AttachmentNotFoundException;
import com.payv.ledger.application.exception.AttachmentStorageFailureException;
import com.payv.ledger.application.exception.AttachmentStorageValidationException;
import com.payv.ledger.application.port.AttachmentStoragePort;
import com.payv.ledger.application.query.TransactionQueryService;
import com.payv.ledger.domain.model.Attachment;
import com.payv.ledger.domain.model.AttachmentId;
import com.payv.ledger.domain.model.TransactionId;
import com.payv.ledger.domain.model.TransferId;
import com.payv.ledger.presentation.dto.request.CreateTransactionRequest;
import com.payv.ledger.presentation.dto.request.UpdateTransactionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/ledger/transactions")
@RequiredArgsConstructor
public class TransactionApiController {

    private final TransactionQueryService queryService;
    private final TransactionCommandService commandService;
    private final TransferCommandService transferCommandService;
    private final AttachmentCommandService attachmentCommandService;
    private final AttachmentStoragePort attachmentStoragePort;

    @PostMapping(produces = "application/json")
    public ResponseEntity<Map<String, Object>> create(@AuthenticationPrincipal IamUserDetails userDetails,
                                                       @ModelAttribute CreateTransactionRequest request) {
        String ownerUserId = userDetails.getUserId();
        if (request.isTransferType()) {
            TransferId id = transferCommandService.create(request.toTransferCommand(), ownerUserId);
            return AjaxResponses.okRedirect("/ledger/transfers/" + id.getValue() + "?created=true");
        }

        TransactionId id = commandService.createManual(request.toCommand(), ownerUserId);
        return AjaxResponses.okRedirect("/ledger/transactions/" + id.getValue() + "?created=true");
    }

    @PutMapping(path = "/{transactionId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Map<String, Object>> update(@AuthenticationPrincipal IamUserDetails userDetails,
                                                       @PathVariable String transactionId,
                                                       @RequestBody UpdateTransactionRequest request) {
        String ownerUserId = userDetails.getUserId();
        if (request.isTransferType()) {
            transferCommandService.update(TransferId.of(transactionId), request.toTransferCommand(), ownerUserId);
            return AjaxResponses.okRedirect("/ledger/transfers/" + transactionId + "?updated=true");
        }

        commandService.updateTransaction(TransactionId.of(transactionId), request.toCommand(), ownerUserId);
        return AjaxResponses.okRedirect("/ledger/transactions/" + transactionId + "?updated=true");
    }

    @DeleteMapping(path = "/{transactionId}", produces = "application/json")
    public ResponseEntity<Map<String, Object>> delete(@AuthenticationPrincipal IamUserDetails userDetails,
                                                       @PathVariable String transactionId) {
        String ownerUserId = userDetails.getUserId();
        commandService.deleteTransaction(TransactionId.of(transactionId), ownerUserId);
        return AjaxResponses.okRedirect("/ledger/transactions?deleted=true");
    }

    @PostMapping(path = "/{transactionId}/attachments", consumes = "multipart/form-data", produces = "application/json")
    public ResponseEntity<Map<String, Object>> uploadAttachment(@AuthenticationPrincipal IamUserDetails userDetails,
                                                                 @PathVariable String transactionId,
                                                                 @RequestParam("file") MultipartFile file) {
        String ownerUserId = userDetails.getUserId();
        attachmentCommandService.upload(TransactionId.of(transactionId), ownerUserId, file);
        return AjaxResponses.okRedirect("/ledger/transactions/" + transactionId + "?attachmentUploaded=true");
    }

    @DeleteMapping(path = "/{transactionId}/attachments/{attachmentId}", produces = "application/json")
    public ResponseEntity<Map<String, Object>> deleteAttachment(@AuthenticationPrincipal IamUserDetails userDetails,
                                                                 @PathVariable String transactionId,
                                                                 @PathVariable String attachmentId) {
        String ownerUserId = userDetails.getUserId();
        attachmentCommandService.delete(AttachmentId.of(attachmentId), ownerUserId);
        return AjaxResponses.okRedirect("/ledger/transactions/" + transactionId + "?attachmentDeleted=true");
    }

    @GetMapping("/{transactionId}/attachments/{attachmentId}/image")
    public ResponseEntity<byte[]> attachmentImage(@AuthenticationPrincipal IamUserDetails userDetails,
                                                  @PathVariable String transactionId,
                                                  @PathVariable String attachmentId) {
        String ownerUserId = userDetails.getUserId();
        try {
            Attachment attachment = queryService.findStoredAttachment(
                            TransactionId.of(transactionId), AttachmentId.of(attachmentId), ownerUserId)
                    .orElseThrow(AttachmentNotFoundException::new);

            byte[] body = attachmentStoragePort.readFinal(
                    attachment.getStoragePath(),
                    attachment.getStoredFileName()
            );
            MediaType mediaType = parseMediaType(attachment.getContentType());
            return ResponseEntity.ok().contentType(mediaType).body(body);
        } catch (AttachmentNotFoundException | AttachmentBinaryNotFoundException | AttachmentStorageValidationException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (AttachmentStorageFailureException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
}
