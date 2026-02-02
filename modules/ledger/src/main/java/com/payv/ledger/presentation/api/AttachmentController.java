package com.payv.ledger.presentation.api;

import com.payv.ledger.application.command.AttachmentCommandService;
import com.payv.ledger.domain.model.AttachmentId;
import com.payv.ledger.domain.model.TransactionId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ledger/transactions/{transactionId}/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentCommandService commandService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<UploadAttachmentResponse> upload(@RequestHeader("X-User-Id") String ownerUserId,
                                                           @PathVariable String transactionId,
                                                           @RequestPart("file") MultipartFile file) {

        AttachmentId id = commandService.upload(TransactionId.of(transactionId), ownerUserId, file);
        // finalize(move)는 커밋 이후 실행되므로 즉시 STORED를 보장하지 않음
        return ResponseEntity.accepted().body(new UploadAttachmentResponse(id.getValue(), "UPLOADING"));
    }

    @Data
    @AllArgsConstructor
    public static class UploadAttachmentResponse {
        private final String attachmentId;
        private final String status;
    }
}
