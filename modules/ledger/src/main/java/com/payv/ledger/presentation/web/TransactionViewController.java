package com.payv.ledger.presentation.web;

import com.payv.ledger.application.command.TransactionCommandService;
import com.payv.ledger.application.command.model.CreateTransactionCommand;
import com.payv.ledger.application.query.TransactionQueryService;
import com.payv.ledger.domain.model.Transaction;
import com.payv.ledger.domain.model.TransactionId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RequestMapping("/ledger/transactions")
@RequiredArgsConstructor
public class TransactionViewController {

    private final TransactionQueryService queryService;

    @GetMapping
    public String list(@RequestHeader("X-User-Id") String ownerUserId,
                       @RequestParam(required = false) String from,
                       @RequestParam(required = false) String to,
                       @RequestParam(required = false) String assetId,
                       @RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "20") int size,
                       Model model) {

        LocalDate fromDate = (from != null && !from.isEmpty()) ? LocalDate.parse(from) : null;
        LocalDate toDate = (to != null && !to.isEmpty()) ? LocalDate.parse(to) : null;

        model.addAttribute("result", queryService.list(ownerUserId, fromDate, toDate, assetId, page, size));
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("assetId", assetId);

        return "ledger/transaction/list";
    }

    @GetMapping("/{transactionId}")
    public String detail(@RequestHeader("X-User-Id") String ownerUserId,
                         @PathVariable String transactionId,
                         Model model) {

        model.addAttribute("tx", queryService.detail(TransactionId.of(transactionId), ownerUserId));
        return "ledger/transaction/detail";
    }
}
