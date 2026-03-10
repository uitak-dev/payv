package com.payv.ledger.presentation.api;

import com.payv.common.presentation.api.AjaxResponses;
import com.payv.iam.infrastructure.security.IamUserDetails;
import com.payv.ledger.application.command.TransferCommandService;
import com.payv.ledger.domain.model.TransferId;
import com.payv.ledger.presentation.dto.request.CreateTransferRequest;
import com.payv.ledger.presentation.dto.request.UpdateTransferRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ledger/transfers")
@RequiredArgsConstructor
public class TransferApiController {

    private final TransferCommandService commandService;

    @PostMapping(produces = "application/json")
    public ResponseEntity<Map<String, Object>> create(@AuthenticationPrincipal IamUserDetails userDetails,
                                                       @ModelAttribute CreateTransferRequest request) {
        String ownerUserId = userDetails.getUserId();
        TransferId id = commandService.create(request.toCommand(), ownerUserId);
        return AjaxResponses.okRedirect("/ledger/transfers/" + id.getValue() + "?created=true");
    }

    @PutMapping(path = "/{transferId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Map<String, Object>> update(@AuthenticationPrincipal IamUserDetails userDetails,
                                                       @PathVariable String transferId,
                                                       @RequestBody UpdateTransferRequest request) {
        String ownerUserId = userDetails.getUserId();
        commandService.update(TransferId.of(transferId), request.toCommand(), ownerUserId);
        return AjaxResponses.okRedirect("/ledger/transfers/" + transferId + "?updated=true");
    }

    @DeleteMapping(path = "/{transferId}", produces = "application/json")
    public ResponseEntity<Map<String, Object>> delete(@AuthenticationPrincipal IamUserDetails userDetails,
                                                       @PathVariable String transferId) {
        String ownerUserId = userDetails.getUserId();
        commandService.delete(TransferId.of(transferId), ownerUserId);
        return AjaxResponses.okRedirect("/ledger/transfers?deleted=true");
    }
}
