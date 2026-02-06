package com.payv.ledger.presentation.api;

import com.payv.ledger.application.command.TransactionCommandService;
import com.payv.ledger.application.command.model.CreateTransactionCommand;
import com.payv.ledger.application.command.model.UpdateTransactionCommand;
import com.payv.ledger.domain.model.Money;
import com.payv.ledger.domain.model.TransactionId;
import com.payv.ledger.domain.model.TransactionType;
import com.payv.ledger.presentation.dto.request.CreateTransactionRequest;
import com.payv.ledger.presentation.dto.request.UpdateTransactionRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ledger/transactions")
@RequiredArgsConstructor
public class TransactionApiController {

    private final TransactionCommandService transactionCommandService;

    @PostMapping
    public ResponseEntity<CreateTransactionResponse> create(@RequestHeader("X-User-Id") String ownerUserId,
                                                            @RequestBody CreateTransactionRequest req) {

        CreateTransactionCommand command = CreateTransactionCommand.builder()
                        .transactionType(TransactionType.valueOf(req.getTransactionType()))
                        .amount(Money.generate(req.getAmount()))
                        .transactionDate(req.getTransactionDate())
                        .assetId(req.getAssetId())
                        .categoryIdLevel1(req.getCategoryIdLevel1())
                        .categoryIdLevel2(req.getCategoryIdLevel2())
                        .memo(req.getMemo())
                        .build();

        for (String tagId : req.getTagIds()) {
            command.addTagId(tagId);
        }

        TransactionId id = transactionCommandService.createManual(command, ownerUserId);
        return ResponseEntity.status(201).body(new CreateTransactionResponse(id.getValue()));
    }

    @PutMapping("/{transactionId}")
    public ResponseEntity<Void> update(@RequestHeader("X-User-Id") String ownerUserId,
                                       @PathVariable String transactionId,
                                       @RequestBody UpdateTransactionRequest req) {

        UpdateTransactionCommand command = UpdateTransactionCommand.builder()
                .transactionType(TransactionType.valueOf(req.getTransactionType()))
                .amount(Money.generate(req.getAmount()))
                .transactionDate(req.getTransactionDate())
                .assetId(req.getAssetId())
                .categoryIdLevel1(req.getCategoryIdLevel1())
                .categoryIdLevel2(req.getCategoryIdLevel2())
                .memo(req.getMemo())
                .build();

        if (req.getTagIds() != null) {
            for (String tagId : req.getTagIds()) {
                command.addTagId(tagId);
            }
        }

        transactionCommandService.updateTransaction(TransactionId.of(transactionId), command, ownerUserId);
        return ResponseEntity.noContent().build();
    }

    @Data
    @AllArgsConstructor
    public static class CreateTransactionResponse {
        private final String transactionId;
    }
}
