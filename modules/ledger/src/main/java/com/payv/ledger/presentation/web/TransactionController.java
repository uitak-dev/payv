package com.payv.ledger.presentation.web;

import com.payv.ledger.application.command.TransactionCommandService;
import com.payv.ledger.application.command.model.CreateTransactionCommand;
import com.payv.ledger.application.query.TransactionQueryService;
import com.payv.ledger.domain.model.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ledgers")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionCommandService commandService;
    private final TransactionQueryService queryService;

    @PostMapping
    public ResponseEntity<String> create(@RequestBody CreateTransactionCommand command) {
        Transaction transaction = commandService.createTransaction(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction.getId().getValue());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getById(@PathVariable String id) {

        System.out.println("======= " + id + " ========");

        Transaction transaction = queryService.getTransaction(id);
        return ResponseEntity.ok(transaction);
    }

    /**
     * Request body for creating a transaction.
     *
    @Data
    public static class CreateTransactionRequest {
        private LocalDate transactionDate;
        private String memo;
    }
    */
}
