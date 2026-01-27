package com.payv.ledger.presentation.web;

import com.payv.ledger.application.command.TransactionCommandService;
import com.payv.ledger.application.command.model.CreateTransactionCommand;
import com.payv.ledger.application.query.TransactionQueryService;
import com.payv.ledger.domain.model.Transaction;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/ledgers")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionCommandService commandService;
    private final TransactionQueryService queryService;

    @PostMapping
    public ResponseEntity<String> create(@RequestBody CreateTransactionCommand command) {
        Transaction transaction = commandService.create(command);
        return ResponseEntity.ok(transaction.getId().toString());
    }


    @Data
    public static class CreateTransactionRequest {
        private LocalDate transactionDate;
        private String memo;
    }
}
